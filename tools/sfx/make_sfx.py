"""Synthesizes the app's sound effects into app/src/main/res/raw as small 16-bit mono WAVs.

Everything is generated from sine/triangle/noise primitives, so there are no licensing questions.
Usage: python tools/sfx/make_sfx.py   (needs numpy)
"""
import wave
from pathlib import Path

import numpy as np

SR = 44100
OUT = Path(__file__).resolve().parents[2] / "app" / "src" / "main" / "res" / "raw"
rng = np.random.default_rng(7)


def t(dur):
    return np.arange(int(SR * dur)) / SR


def env(n, attack=0.004, release=None, decay=None):
    """Attack ramp plus either exponential decay (decay = time constant) or a linear release tail."""
    x = np.arange(n) / SR
    e = np.minimum(1.0, x / max(attack, 1e-4))
    if decay:
        e *= np.exp(-x / decay)
    if release:
        tail = int(SR * release)
        if 0 < tail < n:
            e[-tail:] *= np.linspace(1, 0, tail)
    return e


def tone(freq, dur, kind="sine", decay=0.12, attack=0.004, vibrato=0.0, vib_rate=7.0, glide_to=None):
    x = t(dur)
    f = np.full_like(x, freq) if glide_to is None else np.geomspace(freq, glide_to, len(x))
    if vibrato:
        f = f * (1 + vibrato * np.sin(2 * np.pi * vib_rate * x))
    phase = 2 * np.pi * np.cumsum(f) / SR
    if kind == "sine":
        w = np.sin(phase)
    elif kind == "bell":  # soft chime: fundamental plus quickly-fading inharmonic partial
        w = np.sin(phase) + 0.35 * np.sin(2.76 * phase) * np.exp(-x / 0.05) + 0.15 * np.sin(5.4 * phase) * np.exp(-x / 0.03)
    elif kind == "chip":  # rounded square-ish tone, like a friendly game console
        w = np.sin(phase) + 0.28 * np.sin(3 * phase) + 0.12 * np.sin(5 * phase)
    elif kind == "tri":
        w = 2 / np.pi * np.arcsin(np.sin(phase))
    else:
        raise ValueError(kind)
    return w * env(len(x), attack=attack, decay=decay, release=min(0.02, dur / 3))


def noise(dur, decay=0.05, lowpass=0.2, highpass=0.0):
    n = rng.standard_normal(int(SR * dur))
    out = np.zeros_like(n)
    acc = 0.0
    for i, v in enumerate(n):  # one-pole low-pass
        acc += lowpass * (v - acc)
        out[i] = acc
    if highpass:
        out = out - np.convolve(out, np.ones(int(highpass)) / int(highpass), mode="same")
    return out * env(len(n), attack=0.002, decay=decay)


def seq(*parts, gap=0.0):
    """Concatenate with an optional gap; parts may be (signal, offset_seconds) to overlap."""
    total = 0
    placed = []
    cursor = 0.0
    for p in parts:
        sig, at = (p if isinstance(p, tuple) else (p, None))
        start = int(SR * (cursor if at is None else at))
        placed.append((start, sig))
        total = max(total, start + len(sig))
        if at is None:
            cursor += len(sig) / SR + gap
    out = np.zeros(total)
    for start, sig in placed:
        out[start:start + len(sig)] += sig
    return out


def mix(*signals):
    out = np.zeros(max(len(s) for s in signals))
    for s in signals:
        out[:len(s)] += s
    return out


def echo(x, delay=0.09, feedback=0.28, taps=3):
    d = int(SR * delay)
    out = np.concatenate([x, np.zeros(d * taps)])
    for k in range(1, taps + 1):
        out[d * k:d * k + len(x)] += x * (feedback ** k)
    return out


def write(name, x, gain_db=-3.0):
    x = x / (np.max(np.abs(x)) + 1e-9) * (10 ** (gain_db / 20))
    fade = min(len(x), int(SR * 0.004))
    x[:fade] *= np.linspace(0, 1, fade)
    x[-fade:] *= np.linspace(1, 0, fade)
    OUT.mkdir(parents=True, exist_ok=True)
    with wave.open(str(OUT / f"{name}.wav"), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes((x * 32767).astype(np.int16).tobytes())
    print(f"{name}.wav  {len(x) / SR:.2f}s")


def main():
    write("sfx_tap", mix(tone(1150, 0.05, decay=0.012, glide_to=820), 0.25 * noise(0.02, decay=0.004, lowpass=0.5)), -12)
    write("sfx_pop", tone(420, 0.09, decay=0.03, glide_to=980), -8)
    write("sfx_coin", echo(seq(tone(988, 0.07, "chip", decay=0.05), tone(1319, 0.32, "chip", decay=0.09)), 0.07, 0.2, 2), -7)
    write("sfx_coins", echo(seq(*[tone(1047 * 2 ** (k / 12), 0.09, "chip", decay=0.04) for k in (0, 4, 7, 12, 16)], gap=0.01), 0.08, 0.25), -7)
    write("sfx_correct", echo(seq(tone(1047, 0.08, "bell", decay=0.08), tone(1319, 0.08, "bell", decay=0.08),
                                  tone(1568, 0.34, "bell", decay=0.16), (0.24 * tone(2093, 0.3, decay=0.12, vibrato=0.004), 0.2)),
                              0.1, 0.22), -5)
    write("sfx_wrong", seq(tone(392, 0.13, "tri", decay=0.09), tone(311, 0.24, "tri", decay=0.12), gap=0.02), -7)
    write("sfx_purchase", echo(seq(0.6 * noise(0.03, decay=0.01, lowpass=0.6), tone(1175, 0.06, "chip", decay=0.04),
                                   tone(1568, 0.3, "bell", decay=0.12)), 0.08, 0.22), -6)
    write("sfx_eat", seq(*[k * noise(0.05, decay=0.018, lowpass=0.35, highpass=6) for k in (1.0, 0.8, 0.65)], gap=0.07), -8)
    write("sfx_love", seq(tone(700, 0.22, decay=0.2, glide_to=1400, vibrato=0.02, vib_rate=11),
                          (0.3 * tone(2349, 0.2, "bell", decay=0.08), 0.16)), -8)
    write("sfx_whoosh", noise(0.26, decay=0.12, lowpass=0.08) * np.sin(np.linspace(0, np.pi, int(SR * 0.26))), -10)
    write("sfx_fanfare", echo(seq(*[tone(f, 0.1, "chip", decay=0.1) for f in (523, 659, 784)],
                                  sum(tone(f, 0.7, "chip", decay=0.35) for f in (523, 659, 784, 1047)), gap=0.015),
                              0.11, 0.25), -5)
    write("sfx_chest", seq(0.5 * noise(0.18, decay=0.1, lowpass=0.05),
                           *[tone(1319 * 2 ** (k / 12), 0.08, "bell", decay=0.06) for k in (0, 3, 7, 12)], gap=0.0), -6)
    write("sfx_levelup", echo(seq(*[tone(523 * 2 ** (k / 12), 0.07, "chip", decay=0.06) for k in (0, 4, 7, 12, 16, 19, 24)],
                                  (0.3 * tone(2093, 0.5, "bell", decay=0.2, vibrato=0.005), 0.45)), 0.1, 0.25), -5)
    write("sfx_tick", tone(2000, 0.018, decay=0.005), -16)


if __name__ == "__main__":
    main()
