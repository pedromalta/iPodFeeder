#!/usr/bin/env python3.11
"""
Generate a minimalistic iPod 3G icon for iPodFeeder macOS app.
The iPod 3G (2003) featured:
  - White rectangular body with rounded corners
  - Small LCD screen at the top
  - 4 touch-sensitive buttons in a row below the screen
  - Large mechanical scroll wheel with a center button
"""

from PIL import Image, ImageDraw, ImageFilter
import math

SIZE = 1024

def make_icon(size):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))

    scale = size / 1024

    def s(v):
        return int(v * scale)

    # ── Body dimensions ────────────────────────���─────────────────────────────
    BW, BH = s(620), s(860)
    BX = (size - BW) // 2
    BY = (size - BH) // 2
    CR = s(100)  # corner radius

    # ── Drop shadow ──────────────────────────────────────────────────────────
    shadow_layer = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow_layer)
    sd.rounded_rectangle(
        [BX + s(10), BY + s(16), BX + BW + s(10), BY + BH + s(16)],
        radius=CR, fill=(0, 0, 0, 160)
    )
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(s(28)))
    img = Image.alpha_composite(img, shadow_layer)
    draw = ImageDraw.Draw(img)

    # ── Body (white with very subtle warm-gray tint) ─────────────────────────
    draw.rounded_rectangle(
        [BX, BY, BX + BW, BY + BH],
        radius=CR,
        fill=(242, 242, 240, 255),
        outline=(210, 210, 208, 255),
        width=s(2)
    )

    # ── Screen bezel (dark) ───────────────────────────────────────────────────
    SW, SH = s(380), s(210)
    SX = BX + (BW - SW) // 2
    SY = BY + s(75)
    draw.rounded_rectangle(
        [SX, SY, SX + SW, SY + SH],
        radius=s(16),
        fill=(30, 30, 30, 255)
    )

    # ── Screen content: fake status bar + music note ──────────────────────────
    # Inner screen area (slightly smaller, dark teal/blue-gray like early iPod LCD)
    ISW, ISH = SW - s(18), SH - s(18)
    ISX = SX + s(9)
    ISY = SY + s(9)
    draw.rounded_rectangle(
        [ISX, ISY, ISX + ISW, ISY + ISH],
        radius=s(8),
        fill=(60, 90, 80, 255)  # classic iPod LCD greenish tint
    )

    # ── Scroll wheel ─────────────────────────────────────────────────────────
    WD = s(440)
    WX = BX + (BW - WD) // 2
    WY = SY + SH + s(60)

    # Outer ring (slightly darker)
    draw.ellipse(
        [WX, WY, WX + WD, WY + WD],
        fill=(208, 208, 206, 255)
    )

    # Inner ring highlight (gives the wheel a raised look)
    IRD = WD - s(30)
    IRX = WX + s(15)
    IRY = WY + s(15)
    draw.ellipse(
        [IRX, IRY, IRX + IRD, IRY + IRD],
        fill=(228, 228, 226, 255)
    )

    # Center button
    CD = s(148)
    CX = WX + (WD - CD) // 2
    CY = WY + (WD - CD) // 2
    draw.ellipse(
        [CX, CY, CX + CD, CY + CD],
        fill=(242, 242, 240, 255),
        outline=(200, 200, 198, 255),
        width=s(2)
    )

    # ── Subtle top sheen (highlight) ─────────────────────────────────────────
    sheen = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    sheen_draw = ImageDraw.Draw(sheen)
    sheen_draw.rounded_rectangle(
        [BX + s(4), BY + s(4), BX + BW - s(4), BY + BH // 2],
        radius=CR,
        fill=(255, 255, 255, 38)
    )
    img = Image.alpha_composite(img, sheen)

    return img


# ── Generate and save ────────────────────────────────────────────────────────
icon_1024 = make_icon(1024)
OUT_BASE = "/Users/pedromalta/AndroidStudioProjects/iPodFeeder/app/src"

icon_1024.save(f"{OUT_BASE}/commonMain/composeResources/drawable/icon.png")
icon_1024.save(f"{OUT_BASE}/desktopMain/resources/icon.png")

print("Icon saved at 1024×1024 to both resource locations.")

