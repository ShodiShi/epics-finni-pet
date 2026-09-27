"""Turns raw generated PNGs into app resources.

* Stickers/poses/medals: removes the white background (flood fill from the border, protected by the art's dark
  outline), keeps anti-aliased edges without white halos, trims, scales and exports WebP with alpha.
* Scenes: resized and exported as WebP.
* pet_head: also becomes the adaptive launcher icon foreground in every mipmap density.

Usage: python_embeded\\python.exe tools/art/process.py [names...]
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image
from scipy import ndimage

RAW_DIR = Path(r"C:\local\dev\finni-art\raw")
CUT_DIR = Path(r"C:\local\dev\finni-art\cut")
REPO = Path(__file__).resolve().parents[2]
RES = REPO / "app" / "src" / "main" / "res"
DRAWABLE = RES / "drawable-nodpi"

# All poses share one scale so Finni keeps the same size when switching poses.
POSE_SCALE = 0.72
ICON_MAX = 320
MEDAL_MAX = 420
GOAL_MAX = 480
SCENE_TALL_WIDTH = 1080
SCENE_WIDE_WIDTH = 1216

LAUNCHER_DENSITIES = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}


def non_whiteness(rgb: np.ndarray) -> np.ndarray:
    """0 for pure white, grows with darkness *and* saturation (min channel distance from 255)."""
    return 255.0 - rgb.min(axis=2)


def remove_white_background(img: Image.Image, fill_thresh=48.0, pocket_thresh=10.0, pocket_min_area=0) -> Image.Image:
    rgb = np.asarray(img.convert("RGB")).astype(np.float32)
    nw = non_whiteness(rgb)

    # Everything light enough to be paper can be background, but only where it connects to the image border.
    # The barrier is closed slightly so hairline gaps in the outline can't leak the fill into light fur.
    barrier = ndimage.binary_closing(nw >= fill_thresh, structure=np.ones((3, 3)), iterations=1)
    candidates = ~barrier
    labels, _ = ndimage.label(candidates)
    border_labels = np.unique(np.concatenate([labels[0, :], labels[-1, :], labels[:, 0], labels[:, -1]]))
    background = np.isin(labels, border_labels[border_labels > 0])

    # Soft grey ground shadows touching the paper: fur and props are saturated or dark, shadows are light and neutral.
    saturation = rgb.max(axis=2) - rgb.min(axis=2)
    shadow = (saturation < 32) & (rgb.min(axis=2) > 165)
    background = ndimage.binary_propagation(background, mask=background | shadow)

    # Enclosed pockets of pure paper white (e.g. between an arm and the body) are background too.
    if pocket_min_area > 0:
        pocket_labels, count = ndimage.label((nw < pocket_thresh) & ~background)
        if count:
            sizes = ndimage.sum(np.ones_like(nw), pocket_labels, index=np.arange(1, count + 1))
            big = np.where(sizes >= pocket_min_area)[0] + 1
            background |= np.isin(pocket_labels, big)

    alpha = np.where(background, 0.0, 1.0)

    # Anti-aliased rim: a thin band around the boundary gets alpha from how "inky" the pixel is, and its colour is
    # un-mixed from the white paper so the edge doesn't glow.
    rim = ndimage.binary_dilation(background, iterations=2) & ndimage.binary_dilation(~background, iterations=2)
    rim_alpha = np.clip((nw - 6.0) / 170.0, 0.0, 1.0)
    alpha = np.where(rim, rim_alpha, alpha)

    a = alpha[..., None]
    safe = np.maximum(a, 1e-3)
    unmixed = np.clip((rgb - (1.0 - a) * 255.0) / safe, 0, 255)
    out_rgb = np.where(a > 0, unmixed, 0)

    rgba = np.dstack([out_rgb, alpha * 255.0]).round().astype(np.uint8)
    return Image.fromarray(rgba, "RGBA")


def trim(img: Image.Image, pad: int) -> Image.Image:
    bbox = img.getchannel("A").point(lambda v: 255 if v > 8 else 0).getbbox()
    if not bbox:
        return img
    left, top, right, bottom = bbox
    left, top = max(0, left - pad), max(0, top - pad)
    right, bottom = min(img.width, right + pad), min(img.height, bottom + pad)
    return img.crop((left, top, right, bottom))


def fit(img: Image.Image, max_side: int) -> Image.Image:
    scale = min(1.0, max_side / max(img.size))
    if scale >= 1.0:
        return img
    return img.resize((round(img.width * scale), round(img.height * scale)), Image.LANCZOS)


def scaled(img: Image.Image, scale: float) -> Image.Image:
    return img.resize((round(img.width * scale), round(img.height * scale)), Image.LANCZOS)


def save_webp(img: Image.Image, name: str, quality: int = 90):
    DRAWABLE.mkdir(parents=True, exist_ok=True)
    img.save(DRAWABLE / f"{name}.webp", "WEBP", quality=quality, method=6)


def kind_of(name: str) -> str:
    if name.startswith("bg_"):
        return "scene"
    if name == "pet_head":
        return "head"
    if name.startswith("pet_"):
        return "pose"
    if name.startswith("badge_"):
        return "medal"
    if name.startswith(("goal_", "share_")):
        return "goal"
    return "icon"


def launcher_icon(cut: Image.Image):
    # Adaptive icons show roughly the middle 66% of the 108dp canvas. The standing fox is scaled so its head fills
    # that window; the canvas edge cuts the body off below the visible mask.
    fox = trim(cut, 0)
    for density, size in LAUNCHER_DENSITIES.items():
        canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        width = round(size * 0.55)
        icon = fox.resize((width, round(fox.height * width / fox.width)), Image.LANCZOS)
        top = round(size * 0.235)
        icon = icon.crop((0, 0, icon.width, min(icon.height, size - top)))
        canvas.alpha_composite(icon, ((size - icon.width) // 2, top))
        out_dir = RES / f"mipmap-{density}"
        out_dir.mkdir(parents=True, exist_ok=True)
        canvas.save(out_dir / "ic_launcher_foreground.webp", "WEBP", quality=95, method=6)


def process(name: str):
    raw = Image.open(RAW_DIR / f"{name}.png").convert("RGB")
    kind = kind_of(name)
    if kind == "scene":
        width = SCENE_TALL_WIDTH if raw.height > raw.width else SCENE_WIDE_WIDTH
        img = raw.resize((width, round(raw.height * width / raw.width)), Image.LANCZOS)
        save_webp(img, name, quality=86)
        return

    pocket = 2500 if kind in ("pose", "head") else 0
    cut = remove_white_background(raw, pocket_min_area=pocket)
    CUT_DIR.mkdir(parents=True, exist_ok=True)
    cut.save(CUT_DIR / f"{name}.png")

    if kind == "head":
        launcher_icon(cut)  # the head only feeds the launcher icon; the app never draws it
    elif kind == "pose":
        save_webp(scaled(trim(cut, 6), POSE_SCALE), name)
    elif kind == "medal":
        save_webp(fit(trim(cut, 4), MEDAL_MAX), name)
    elif kind == "goal":
        save_webp(fit(trim(cut, 4), GOAL_MAX), name)
    else:
        save_webp(fit(trim(cut, 4), ICON_MAX), name)


def main(argv):
    names = argv or sorted(p.stem for p in RAW_DIR.glob("*.png"))
    for name in names:
        process(name)
        print("ok", name, flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
