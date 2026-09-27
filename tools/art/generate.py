"""Generates every illustrated asset for the app through the local ComfyUI (FLUX.2 Klein).

Usage (ComfyUI must be running):
    python_embeded\\python.exe tools/art/generate.py            # generate everything that is missing
    python_embeded\\python.exe tools/art/generate.py pet_idle    # (re)generate specific assets
    python_embeded\\python.exe tools/art/generate.py --seed 7 pet_idle

Raw PNGs land in RAW_DIR (outside the repo); tools/art/process.py turns them into app resources.
"""
import shutil
import sys
import time
from pathlib import Path

# ComfyUI's embedded interpreter runs isolated (._pth), so the script folder isn't on sys.path by default.
sys.path.insert(0, str(Path(__file__).resolve().parent))
import comfy_client as comfy  # noqa: E402

RAW_DIR = Path(r"C:\local\dev\finni-art\raw")
FOX_REF_SRC = Path(r"C:\ComfyUI\ComfyUI_windows_portable\ComfyUI\output\chat\img_00026_.png")

POSE = ("The same cute baby fox character from image 1, {pose}. Keep the exact same character design, proportions, "
        "face, orange and cream colors, thick dark brown outlines and flat vector cartoon sticker style. "
        "Full body, centered, pure white background, no shadow, no ground, no text.")

STICKER = ("{what}. Cute cartoon game icon for a children's mobile game, flat vector sticker illustration, thick dark brown "
           "outline, soft cel shading with a small highlight, simple rounded shapes, vibrant warm colors, single object, "
           "centered, isolated on a pure white background, no shadow, no text, no letters.")

MEDAL = ("A round shiny game achievement medal: thick gold rim, {color} enamel center with a {emblem} in the middle, "
         "a short {color} ribbon on top. Cute cartoon game icon, flat vector sticker illustration, thick dark brown outline, "
         "soft cel shading, centered, isolated on a pure white background, no shadow, no text, no letters, no numbers.")

SCENE = ("{what}. Children's mobile game background art, flat vector illustration with soft gradients, warm pastel palette "
         "(peach, cream, soft orange, mint green, sky blue), cozy and cheerful, clean composition, high detail, "
         "no characters, no people, no animals, no text, no letters.")

ASSETS = [
    # ---- Finni poses (character reference) -----------------------------------------------------------------------
    ("pet_idle", "pose", "standing upright on two legs facing the viewer, relaxed happy closed-mouth smile, arms relaxed at its sides"),
    ("pet_happy", "pose", "standing and laughing with joy, both arms raised up, big open-mouth smile, eyes happily closed like upside-down U shapes"),
    ("pet_sad", "pose", "standing with a sad face, ears drooping down, big teary eyes, small frown, shoulders slumped, looking down"),
    ("pet_hungry", "pose", "standing and holding its tummy with both paws, hungry pleading face, licking its lips with a small pink tongue"),
    ("pet_eating", "pose", "standing and cheerfully chewing with its mouth closed, cheeks slightly round, eyes happily closed, one paw rubbing its tummy"),
    ("pet_play", "pose", "jumping high in the air with joy, legs tucked, both arms spread wide, excited open-mouth smile"),
    ("pet_love", "pose", "standing with eyes closed and a blissful smile, rosy blushing cheeks, both paws clasped together near its chest"),
    ("pet_sleep", "pose", "curled up asleep in a round ball, eyes closed peacefully, fluffy tail wrapped around its body"),
    ("pet_wave", "pose", "waving hello with one paw raised high, big happy open-mouth smile, sparkling eyes"),
    ("pet_think", "pose", "standing with one paw touching its chin, looking up thoughtfully with a curious expression"),
    ("pet_teacher", "pose", "wearing small round glasses and holding an open book in both paws, friendly smile"),
    ("pet_shop", "pose", "holding a small woven shopping basket full of fruit, happy smile"),
    ("pet_piggy", "pose", "hugging a cute pink piggy bank with both arms, happy smile"),
    ("pet_gift", "pose", "holding a small gift box with a red ribbon in both paws, kind gentle smile"),
    ("pet_cheer", "pose", "jumping and cheering with both fists raised high, huge excited open-mouth smile"),
    ("pet_oops", "pose", "scratching the back of its head with one paw, shy embarrassed smile, one eye closed"),
    ("pet_trophy", "pose", "standing proudly and holding a shiny golden trophy cup in front of its chest with both paws, big proud smile"),
    ("pet_head", "pose", "only the head of the fox, front view, big happy smile, no body"),

    # ---- Scenes ------------------------------------------------------------------------------------------------
    ("bg_room", "scene_tall", "Cozy cartoon bedroom for a small pet fox, a round window with a sunny sky and clouds on the back wall, "
                              "a wooden shelf with plants and books, a big round soft rug in the center of the floor, empty open floor space "
                              "in the middle for a character"),
    ("bg_onboarding", "scene_tall", "Sunny meadow with rolling green hills, a tiny cottage far away, blue sky with fluffy clouds, "
                                    "small flowers in the grass, open empty grass in the lower middle"),
    ("bg_school", "scene_wide", "Cozy cartoon classroom with a green chalkboard, a wooden teacher desk with a globe and a stack of books, "
                                "a sunny window, potted plant"),
    ("bg_shop", "scene_wide", "Cute cartoon little shop interior with wooden shelves full of fruit, bread, milk and colorful toys, "
                              "a striped awning at the top, warm light"),
    ("bg_bank", "scene_wide", "Cheerful cartoon treasure room with a round golden vault door, neat stacks of gold coins, "
                              "a big pink piggy bank on a shelf, plants, warm light"),
    ("bg_trophy", "scene_wide", "Cartoon hall of fame with wooden shelves of shiny golden trophies and medals, colorful bunting "
                                "flags, warm spotlight glow"),

    # ---- Shop items ----------------------------------------------------------------------------------------------
    ("item_apple", "sticker", "A shiny red apple with a green leaf"),
    ("item_carrot", "sticker", "A fresh orange carrot with green leaves"),
    ("item_fish", "sticker", "A small cute blue fish, cooked fish meal for a pet"),
    ("item_milk", "sticker", "A small glass bottle of milk with a blue cap"),
    ("item_bread", "sticker", "A golden brown loaf of bread with three diagonal cuts on top, side view"),
    ("item_ball", "sticker", "A bouncy colorful beach ball with red, yellow and blue stripes"),
    ("item_bow", "sticker", "A cute pink ribbon hair bow"),
    ("item_bear", "sticker", "A soft brown teddy bear plush toy sitting"),
    ("item_balloon", "sticker", "A shiny red balloon on a curly string"),
    ("item_cap", "sticker", "A blue baseball cap"),

    # ---- "Need or want?" game cards ----------------------------------------------------------------------------
    ("sort_water", "sticker", "A bottle of clean drinking water"),
    ("sort_jacket", "sticker", "A warm winter jacket with a hood"),
    ("sort_medicine", "sticker", "A small medicine bottle with a red cross and two pills"),
    ("sort_toothbrush", "sticker", "A blue toothbrush with white bristles and a blob of mint toothpaste on the bristles, lying diagonally"),
    ("sort_backpack", "sticker", "A school backpack"),
    ("sort_shoes", "sticker", "A pair of everyday sneakers"),
    ("sort_candy", "sticker", "A big swirly rainbow lollipop"),
    ("sort_car", "sticker", "A shiny red toy race car"),
    ("sort_gamepad", "sticker", "A video game controller"),
    ("sort_icecream", "sticker", "An ice cream cone with pink and white scoops"),
    ("sort_stickers", "sticker", "A rectangular sheet of paper covered with colorful shiny star and heart stickers"),
    ("sort_robot", "sticker", "A cute boxy silver toy robot with a round antenna and blue buttons"),

    # ---- UI icons ------------------------------------------------------------------------------------------------
    ("ic_coin", "sticker", "A shiny round gold coin with a small paw print embossed in the center"),
    ("ic_coins_pile", "sticker", "A small pile of shiny gold coins"),
    ("ic_chest", "sticker", "A closed wooden treasure chest with gold trim"),
    ("ic_chest_open", "sticker", "An open wooden treasure chest overflowing with gold coins and glowing sparkles"),
    ("ic_piggy", "sticker", "A cute pink piggy bank with a coin slot on top, a plain gold coin going into the slot, no symbols or signs on the coin"),
    ("ic_gift", "sticker", "A gift box wrapped in blue paper with a red ribbon bow"),
    ("ic_heart", "sticker", "A glossy red heart"),
    ("ic_star", "sticker", "A shiny golden five-pointed star"),
    ("ic_flame", "sticker", "A bright orange and yellow flame"),
    ("ic_house", "sticker", "A tiny cozy house with a red roof and a round window"),
    ("ic_book", "sticker", "An open book with a small golden star above it"),
    ("ic_bag", "sticker", "A cute shopping bag with handles"),
    ("ic_trophy", "sticker", "A shiny golden trophy cup"),
    ("ic_bowl", "sticker", "An empty pet food bowl"),
    ("ic_seedling", "sticker", "A tiny green sprout growing from a gold coin"),

    # ---- Savings goals (also appear in Finni's room once bought) ------------------------------------------------
    ("goal_bed", "sticker", "A cozy round pet bed with raised soft sides and a blue cushion inside, three-quarter view"),
    ("goal_zoo", "sticker", "A framed cartoon poster of a friendly lion"),
    ("goal_bike", "sticker", "A small kids bicycle with a basket"),
    ("goal_house", "sticker", "A cute wooden pet house with a round door and a red roof"),

    # ---- Sharing goals ---------------------------------------------------------------------------------------------
    ("share_food", "sticker", "A paper bag of pet food with a paw print on it"),
    ("share_books", "sticker", "A neat stack of colorful books with a small red heart on top"),
    ("share_tree", "sticker", "A young tree sapling in a pot"),

    # ---- Badges ----------------------------------------------------------------------------------------------------
    ("badge_first_step", "medal", ("orange", "plain gold coin with a small paw print, no currency signs")),
    ("badge_saver_bronze", "medal", ("pink", "piggy bank")),
    ("badge_saver_gold", "medal", ("purple", "money bag")),
    ("badge_balanced", "medal", ("teal", "balance scale")),
    ("badge_streak_3", "medal", ("red", "flame")),
    ("badge_streak_7", "medal", ("blue", "star")),
    ("badge_quiz_master", "medal", ("green", "graduation cap")),
    ("badge_kind_heart", "medal", ("rose", "heart")),
    ("badge_sorter", "medal", ("yellow", "shopping basket")),

    # ---- Settings (appended so the seeds of the assets above stay put) ---------------------------------------------
    ("ic_music", "sticker", "Two shiny purple musical eighth notes joined by a beam"),
    ("ic_sound", "sticker", "A classic volume speaker symbol: a small sky-blue box with a wide cone flaring out to the right, "
                            "and three curved sound wave arcs to the right of the cone, side view"),
]

SIZES = {"pose": (1024, 1024), "sticker": (1024, 1024), "medal": (1024, 1024),
         "scene_tall": (768, 1344), "scene_wide": (1216, 832)}


def build(name, kind, spec, seed, fox_ref):
    w, h = SIZES[kind]
    if kind == "pose":
        return comfy.klein_graph(POSE.format(pose=spec), w, h, seed, name, refs=[fox_ref])
    if kind == "medal":
        color, emblem = spec
        return comfy.klein_graph(MEDAL.format(color=color, emblem=emblem), w, h, seed, name)
    if kind == "sticker":
        return comfy.klein_graph(STICKER.format(what=spec), w, h, seed, name)
    return comfy.klein_graph(SCENE.format(what=spec), w, h, seed, name)


def main(argv):
    seed = None
    if "--seed" in argv:
        i = argv.index("--seed")
        seed = int(argv[i + 1])
        del argv[i:i + 2]
    wanted = set(argv)
    RAW_DIR.mkdir(parents=True, exist_ok=True)
    fox_ref = comfy.stage_ref(FOX_REF_SRC, "fox_base.png")

    for idx, (name, kind, spec) in enumerate(ASSETS):
        target = RAW_DIR / f"{name}.png"
        if wanted and name not in wanted:
            continue
        if not wanted and target.exists():
            continue
        started = time.time()
        out = comfy.run(build(name, kind, spec, seed if seed is not None else 1000 + idx, fox_ref))
        shutil.copyfile(out, target)
        print(f"{name}: {time.time() - started:.1f}s", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
