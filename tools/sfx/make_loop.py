"""Turns a generated music clip into a seamless background loop (res/raw/music_loop.wav).

Detects the beat period from the onset envelope, cuts a whole number of bars starting on a strong onset, and
crossfades the audio just past the loop end into the loop head, so wrapping around is inaudible. The result is a
16-bit mono WAV that the app plays gaplessly with a static AudioTrack.

Usage: python_embeded\\python.exe tools/sfx/make_loop.py <clip.flac>
"""
import sys
import wave
from pathlib import Path

import av
import numpy as np

OUT = Path(__file__).resolve().parents[2] / "app" / "src" / "main" / "res" / "raw" / "music_loop.wav"
TRIM_HEAD = 0.35  # the first moments of a generated clip are often noisy


def load_mono(path: str):
    container = av.open(path)
    stream = container.streams.audio[0]
    resampler = av.AudioResampler(format="flt", layout="mono", rate=stream.rate)
    chunks = []
    for frame in container.decode(stream):
        for out in resampler.resample(frame):
            chunks.append(out.to_ndarray().reshape(-1))
    return np.concatenate(chunks).astype(np.float64), stream.rate


def onset_envelope(x, sr, hop=256, n=1024):
    window = np.hanning(n)
    frames = np.array([np.abs(np.fft.rfft(x[i:i + n] * window)) for i in range(0, len(x) - n, hop)])
    flux = np.maximum(np.diff(np.log1p(frames), axis=0), 0).sum(axis=1)
    flux = (flux - flux.mean()) / (flux.std() + 1e-9)
    return np.concatenate([[0.0], flux]), sr / hop


def beat_period(env, rate):
    ac = np.correlate(env, env, mode="full")[len(env) - 1:]
    lo, hi = int(0.28 * rate), int(1.1 * rate)
    lag = lo + int(np.argmax(ac[lo:hi]))
    return lag / rate


def main(path):
    x, sr = load_mono(path)
    x = x[int(TRIM_HEAD * sr):]
    env, rate = onset_envelope(x, sr)
    beat = beat_period(env, rate)

    first_beat = env[: int(beat * rate)]
    start = int(np.argmax(first_beat) / rate * sr)
    crossfade = int(min(0.25, beat / 2) * sr)
    bar = beat * 4
    usable = (len(x) - start - crossfade) / sr
    bars = int(usable // bar)
    if bars < 1:
        raise SystemExit(f"clip too short for a bar of {bar:.2f}s")
    length = int(round(bars * bar * sr))

    loop = x[start:start + length].copy()
    tail = x[start + length:start + length + crossfade]
    t = np.linspace(0, np.pi / 2, crossfade)
    loop[:crossfade] = loop[:crossfade] * np.sin(t) + tail * np.cos(t)

    rms = np.sqrt(np.mean(loop ** 2))
    loop *= 10 ** (-20 / 20) / (rms + 1e-9)
    peak = np.max(np.abs(loop))
    if peak > 10 ** (-1 / 20):
        loop *= 10 ** (-1 / 20) / peak

    OUT.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(OUT), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(sr)
        w.writeframes((np.clip(loop, -1, 1) * 32767).astype(np.int16).tobytes())
    print(f"beat {beat:.3f}s ({60 / beat:.0f} bpm), {bars} bars, loop {length / sr:.2f}s @ {sr}Hz -> {OUT} "
          f"({OUT.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main(sys.argv[1])
