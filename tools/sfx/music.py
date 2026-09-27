"""Generates candidate background-music clips with MiniMax H3 (video + audio model) through the local ComfyUI.

Only the audio latent is decoded (SaveAudio), so the heavy video decode is skipped. The first frame is Finni's
room, so the soundtrack matches a calm, empty, sunny room.

Usage: python_embeded\\python.exe tools/sfx/music.py [seed ...]
"""
import json
import shutil
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "art"))
import comfy_client as comfy  # noqa: E402

OUT_DIR = Path(r"C:\local\dev\finni-art\music")
ROOM = Path(r"C:\local\dev\finni-art\raw\bg_room.png")

H3 = {
    "unet": "minimax_h3_fl2va_pruned_int8_convrot.safetensors",
    "lora": "minimax_h3_fl2v_turbo_8step_v1.0_comfyui_bf16.safetensors",
    "clip": "qwen3vl_32b_minimax_h3_nvfp4_awq.safetensors",
    "vae_video": "minimax_h3_video_vae_fp16.safetensors",
    "vae_audio": "minimax_h3_audio_vae_fp32.safetensors",
    "steps": 8,
    "fps": 24,
}

PROMPT = (
    "A cozy cartoon bedroom for a pet with a round window, warm afternoon sunlight, tiny dust sparkles floating, "
    "a very slow gentle camera drift. Soundtrack: soft, cheerful and calm instrumental background music for a "
    "children's mobile game, a light playful melody on xylophone and marimba with a gentle ukulele strum and soft "
    "chimes, relaxed steady tempo, warm and happy mood. Instrumental only: no singing, no speech, no voices, "
    "no dialogue, no sound effects."
)


def frames_for(seconds: float) -> int:
    n = max(5, round(seconds * H3["fps"]))
    while n % 17 != 5:
        n += 1
    return n


def graph(first_frame: str, seed: int, seconds: float, width=384, height=672, prefix="music"):
    return {
        "unet": {"class_type": "UNETLoader", "inputs": {"unet_name": H3["unet"], "weight_dtype": "default"}},
        "lora": {"class_type": "LoraLoaderModelOnly", "inputs": {"lora_name": H3["lora"], "strength_model": 1, "model": ["unet", 0]}},
        "clip": {"class_type": "CLIPLoader", "inputs": {"clip_name": H3["clip"], "type": "minimax", "device": "default"}},
        "vae_v": {"class_type": "VAELoader", "inputs": {"vae_name": H3["vae_video"]}},
        "vae_a": {"class_type": "VAELoader", "inputs": {"vae_name": H3["vae_audio"]}},
        "noise": {"class_type": "RandomNoise", "inputs": {"noise_seed": seed}},
        "ksel": {"class_type": "KSamplerSelect", "inputs": {"sampler_name": "res_multistep"}},
        "sched": {"class_type": "BasicScheduler", "inputs": {"scheduler": "simple", "steps": H3["steps"], "denoise": 1, "model": ["lora", 0]}},
        "img": {"class_type": "LoadImage", "inputs": {"image": first_frame}},
        "cond": {"class_type": "MiniMaxH3ImageToVideo", "inputs": {
            "prompt": PROMPT, "width": width, "height": height, "length": frames_for(seconds),
            "clip": ["clip", 0], "vae": ["vae_v", 0], "first_frame": ["img", 0]}},
        "guider": {"class_type": "BasicGuider", "inputs": {"model": ["lora", 0], "conditioning": ["cond", 0]}},
        "sampler": {"class_type": "SamplerCustomAdvanced", "inputs": {
            "noise": ["noise", 0], "guider": ["guider", 0], "sampler": ["ksel", 0], "sigmas": ["sched", 0],
            "latent_image": ["cond", 1]}},
        "dec_a": {"class_type": "VAEDecodeAudio", "inputs": {"samples": ["sampler", 0], "vae": ["vae_a", 0]}},
        "save": {"class_type": "SaveAudio", "inputs": {"audio": ["dec_a", 0], "filename_prefix": f"{comfy.OUT_SUB}/{prefix}"}},
    }


def wait_for_files(prompt_id: str, timeout=1800) -> list[Path]:
    deadline = time.time() + timeout
    while time.time() < deadline:
        hist = comfy._request(f"/history/{prompt_id}")
        if prompt_id in hist:
            entry = hist[prompt_id]
            if entry.get("status", {}).get("status_str") == "error":
                raise RuntimeError(json.dumps(entry["status"].get("messages", []))[:3000])
            files = []
            for node_out in entry.get("outputs", {}).values():
                for key in ("audio", "images"):
                    for f in node_out.get(key, []):
                        files.append(comfy.COMFY_ROOT / f.get("type", "output") / f.get("subfolder", "") / f["filename"])
            return files
        time.sleep(3)
    raise TimeoutError(prompt_id)


def main(seeds):
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    first = comfy.stage_ref(ROOM, "room_first_frame.png")
    for seed in seeds:
        started = time.time()
        pid = comfy.queue(graph(first, seed, seconds=12, prefix=f"music_{seed}"))
        files = wait_for_files(pid)
        for f in files:
            shutil.copyfile(f, OUT_DIR / f"music_{seed}{f.suffix}")
        print(f"seed {seed}: {time.time() - started:.0f}s -> {[f.name for f in files]}", flush=True)


if __name__ == "__main__":
    main([int(s) for s in sys.argv[1:]] or [101, 202])
