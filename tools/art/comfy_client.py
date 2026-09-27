"""Minimal ComfyUI API client using the same graphs as the local "Studio" chat UI.

Run with ComfyUI's bundled interpreter:
    C:\\ComfyUI\\ComfyUI_windows_portable\\python_embeded\\python.exe tools/art/generate.py
"""
import json
import shutil
import time
import urllib.request
import uuid
from pathlib import Path

SERVER = "http://127.0.0.1:8188"
COMFY_ROOT = Path(r"C:\ComfyUI\ComfyUI_windows_portable\ComfyUI")
OUT_SUB = "finni"
REF_SUB = "finni_refs"

SCHNELL = {"ckpt": "flux1-schnell-fp8.safetensors", "steps": 4}
KLEIN = {"unet": "flux-2-klein-4b-fp8.safetensors", "clip": "qwen_3_4b.safetensors",
         "vae": "flux2-vae.safetensors", "steps": 4}
KONTEXT = {"unet": "flux1-dev-kontext_fp8_scaled.safetensors", "clip_l": "clip_l.safetensors",
           "t5": "t5xxl_fp8_e4m3fn.safetensors", "vae": "ae.safetensors", "steps": 20, "guidance": 2.5}

CLIENT_ID = str(uuid.uuid4())


def _request(path, payload=None):
    data = json.dumps(payload).encode() if payload is not None else None
    req = urllib.request.Request(SERVER + path, data=data,
                                 headers={"Content-Type": "application/json"} if data else {})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read())


def stage_ref(src: Path, name: str) -> str:
    """Copies a reference image into ComfyUI's input folder and returns its LoadImage name."""
    dest_dir = COMFY_ROOT / "input" / REF_SUB
    dest_dir.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(src, dest_dir / name)
    return f"{REF_SUB}/{name}"


def schnell_graph(prompt, w, h, seed, prefix):
    return {
        "ckpt": {"class_type": "CheckpointLoaderSimple", "inputs": {"ckpt_name": SCHNELL["ckpt"]}},
        "pos": {"class_type": "CLIPTextEncode", "inputs": {"text": prompt, "clip": ["ckpt", 1]}},
        "neg": {"class_type": "CLIPTextEncode", "inputs": {"text": "", "clip": ["ckpt", 1]}},
        "lat": {"class_type": "EmptySD3LatentImage", "inputs": {"width": w, "height": h, "batch_size": 1}},
        "ks": {"class_type": "KSampler", "inputs": {
            "seed": seed, "steps": SCHNELL["steps"], "cfg": 1, "sampler_name": "euler", "scheduler": "simple",
            "denoise": 1, "model": ["ckpt", 0], "positive": ["pos", 0], "negative": ["neg", 0],
            "latent_image": ["lat", 0]}},
        "dec": {"class_type": "VAEDecode", "inputs": {"samples": ["ks", 0], "vae": ["ckpt", 2]}},
        "save": {"class_type": "SaveImage", "inputs": {"filename_prefix": f"{OUT_SUB}/{prefix}", "images": ["dec", 0]}},
    }


def klein_graph(prompt, w, h, seed, prefix, refs=()):
    ref_mp = 1.0 if len(refs) <= 2 else 0.6 if len(refs) <= 4 else 0.3
    g = {
        "unet": {"class_type": "UNETLoader", "inputs": {"unet_name": KLEIN["unet"], "weight_dtype": "default"}},
        "clip": {"class_type": "CLIPLoader", "inputs": {"clip_name": KLEIN["clip"], "type": "flux2", "device": "default"}},
        "vae": {"class_type": "VAELoader", "inputs": {"vae_name": KLEIN["vae"]}},
        "pos0": {"class_type": "CLIPTextEncode", "inputs": {"text": prompt, "clip": ["clip", 0]}},
        "neg0": {"class_type": "ConditioningZeroOut", "inputs": {"conditioning": ["pos0", 0]}},
    }
    pos, neg = "pos0", "neg0"
    for i, ref in enumerate(refs):
        g[f"img{i}"] = {"class_type": "LoadImage", "inputs": {"image": ref}}
        g[f"sc{i}"] = {"class_type": "ImageScaleToTotalPixels", "inputs": {
            "image": [f"img{i}", 0], "upscale_method": "lanczos", "megapixels": ref_mp, "resolution_steps": 16}}
        g[f"enc{i}"] = {"class_type": "VAEEncode", "inputs": {"pixels": [f"sc{i}", 0], "vae": ["vae", 0]}}
        g[f"pos{i + 1}"] = {"class_type": "ReferenceLatent", "inputs": {"conditioning": [pos, 0], "latent": [f"enc{i}", 0]}}
        g[f"neg{i + 1}"] = {"class_type": "ReferenceLatent", "inputs": {"conditioning": [neg, 0], "latent": [f"enc{i}", 0]}}
        pos, neg = f"pos{i + 1}", f"neg{i + 1}"
    g.update({
        "latent": {"class_type": "EmptyFlux2LatentImage", "inputs": {"width": w, "height": h, "batch_size": 1}},
        "sigmas": {"class_type": "Flux2Scheduler", "inputs": {"steps": KLEIN["steps"], "width": w, "height": h}},
        "guider": {"class_type": "CFGGuider", "inputs": {"cfg": 1, "model": ["unet", 0], "positive": [pos, 0], "negative": [neg, 0]}},
        "ksel": {"class_type": "KSamplerSelect", "inputs": {"sampler_name": "euler"}},
        "noise": {"class_type": "RandomNoise", "inputs": {"noise_seed": seed}},
        "sampler": {"class_type": "SamplerCustomAdvanced", "inputs": {
            "noise": ["noise", 0], "guider": ["guider", 0], "sampler": ["ksel", 0],
            "sigmas": ["sigmas", 0], "latent_image": ["latent", 0]}},
        "dec": {"class_type": "VAEDecode", "inputs": {"samples": ["sampler", 0], "vae": ["vae", 0]}},
        "save": {"class_type": "SaveImage", "inputs": {"filename_prefix": f"{OUT_SUB}/{prefix}", "images": ["dec", 0]}},
    })
    return g


def kontext_graph(prompt, ref, seed, prefix):
    return {
        "unet": {"class_type": "UNETLoader", "inputs": {"unet_name": KONTEXT["unet"], "weight_dtype": "default"}},
        "clip": {"class_type": "DualCLIPLoader", "inputs": {
            "clip_name1": KONTEXT["clip_l"], "clip_name2": KONTEXT["t5"], "type": "flux", "device": "default"}},
        "vae": {"class_type": "VAELoader", "inputs": {"vae_name": KONTEXT["vae"]}},
        "img": {"class_type": "LoadImage", "inputs": {"image": ref}},
        "scale": {"class_type": "FluxKontextImageScale", "inputs": {"image": ["img", 0]}},
        "enc": {"class_type": "VAEEncode", "inputs": {"pixels": ["scale", 0], "vae": ["vae", 0]}},
        "pos0": {"class_type": "CLIPTextEncode", "inputs": {"text": prompt, "clip": ["clip", 0]}},
        "ref": {"class_type": "ReferenceLatent", "inputs": {"conditioning": ["pos0", 0], "latent": ["enc", 0]}},
        "pos": {"class_type": "FluxGuidance", "inputs": {"conditioning": ["ref", 0], "guidance": KONTEXT["guidance"]}},
        "neg": {"class_type": "ConditioningZeroOut", "inputs": {"conditioning": ["pos0", 0]}},
        "ks": {"class_type": "KSampler", "inputs": {
            "seed": seed, "steps": KONTEXT["steps"], "cfg": 1, "sampler_name": "euler", "scheduler": "simple",
            "denoise": 1, "model": ["unet", 0], "positive": ["pos", 0], "negative": ["neg", 0],
            "latent_image": ["enc", 0]}},
        "dec": {"class_type": "VAEDecode", "inputs": {"samples": ["ks", 0], "vae": ["vae", 0]}},
        "save": {"class_type": "SaveImage", "inputs": {"filename_prefix": f"{OUT_SUB}/{prefix}", "images": ["dec", 0]}},
    }


def queue(graph) -> str:
    return _request("/prompt", {"prompt": graph, "client_id": CLIENT_ID})["prompt_id"]


def wait(prompt_id, timeout=900) -> list[Path]:
    deadline = time.time() + timeout
    while time.time() < deadline:
        hist = _request(f"/history/{prompt_id}")
        if prompt_id in hist:
            entry = hist[prompt_id]
            status = entry.get("status", {})
            if status.get("status_str") == "error":
                raise RuntimeError(json.dumps(status.get("messages", []))[:2000])
            paths = []
            for node_out in entry.get("outputs", {}).values():
                for img in node_out.get("images", []):
                    paths.append(COMFY_ROOT / img.get("type", "output") / img.get("subfolder", "") / img["filename"])
            return paths
        time.sleep(1.5)
    raise TimeoutError(prompt_id)


def run(graph) -> Path:
    return wait(queue(graph))[0]
