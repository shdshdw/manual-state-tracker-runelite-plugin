# Manual State Tracker

A RuneLite plugin that shows a movable overlay image which **you** switch with hotkeys, so you can
keep track of any state you like: a boss phase you are counting yourself, which prayer you meant to
be on, which step of a rotation you are on, which side of a room to go to next.

The plugin never reads the game world to decide what to show. Which state is active is entirely
your decision, made by pressing a key.

## What it does

- **State sets.** Create as many sets as you want, each with as many states as you want. Rename,
  reorder and delete both freely. One set is active at a time.
- **Any image.** Each state shows text, a searchable game icon (any item, prayer, skill or spell), a PNG of
  your own, an arrow, or a flat colour.
- **One overlay, anywhere.** A single overlay widget you drag wherever you want. RuneLite remembers
  the position.
- **Uniform size.** One size setting applies to every state, so a large PNG and a small one look
  the same on screen.
- **Hotkeys.** Bind a key to each state individually, or bind a single key that cycles through the
  set, or both. Nothing shows until you press a key.

## Using it

Open the plugin's side panel with the toolbar button.

### Sets

The dropdown at the top selects the active set. **New**, **Rename** and **Delete** manage the sets
themselves. Switching sets clears the overlay, since state positions do not carry between sets.

### States

**Add state** appends a state to the active set. For each state you can set:

| Field | Notes |
| --- | --- |
| Name | Free text. Optionally drawn above or below the image, see *State name* in the config. |
| Image | `Text`, `Game icon`, `Built-in image`, `Custom PNG` or `Colour`. |
| Hotkey | Click the button, then press the combination. Click it again to clear it. |

The small preview to the left of the name is clickable: it shows that state on the overlay
immediately, which is handy while setting things up. The reorder and delete buttons are on the
right.

### Text

Type anything and it is drawn as large as it will fit, centred. Two colour buttons open the
standard RuneLite colour picker:

- **Text colour**, the fill.
- **Outline colour**, the edge drawn around the letters, black by default so the text stays
  readable against both light and dark ground. Drag the picker's alpha slider to fully transparent
  if you want no outline at all.

The outline is scaled with the text, so it keeps the same weight whether you type one character or
a whole phrase.

Shorter text is bigger, since the size is whatever fills the overlay. A single character is about as
large as the overlay allows, which makes `1`, `2`, `3` a good way to count phases or steps.

Text is committed when you press Enter or click away, not on every keystroke.

### Game icons

**Choose icon...** opens a search over the game's own icons, so a state can show a real item,
prayer, skill or spell:

- **Items**, every item in the game cache, untradeables included.
- **Prayers**, all 32.
- **Skills**, all 24 plus the total level.
- **Spells**, all 178 across the standard, ancient, lunar and Arceuus spellbooks.

The dialog opens listing **every prayer, skill and spell**, so you can browse them without knowing a
name. Narrow with the type dropdown, or type to search. Items only appear once you have typed two
characters, since there would otherwise be too many to list. Double-click a result or press **Use icon**.

Exact matches sort first, then names starting with what you typed.
The icons come from the game itself rather than being bundled. 

Item names only exist in the game cache, so they are indexed in the background shortly after the
client starts. To make the results more clean, items that have different ID but the exact same name are reduced to a single entry. This may make certain item variations that are actually different but share a name (i.e. bird's nests) also only have a single entry. 

### Built-in images

Shapes the plugin draws itself: **arrows** up, right, down and left, a **looping arrow** for
"repeat" or "back to the start", a **cross** and a **plus**.

They take the same two colour buttons as text, a fill and an outline, so a red cross and a green
plus are a couple of clicks apart. As with text, a fully transparent outline draws none at all.

### Custom PNGs

Custom images live in:

```
.runelite/manual-state-tracker/images/
```

Either drop PNGs in there and press **Refresh**, or press **Import...** to pick a file and have it
copied in for you. **Open image folder** at the bottom of the panel opens the directory.

Images are scaled to fit the configured overlay size while keeping their aspect ratio, so a
non-square PNG is letterboxed rather than stretched.

## Hotkey behaviour

- Pressing a state's own hotkey shows that state. Pressing it again **hides** the overlay.
- **Next state** / **Previous state** step through the active set and wrap around. If nothing is
  showing, *Next state* starts at the first state and *Previous state* at the last.
- **Hide overlay** clears the active state.
- Hotkeys do nothing while you are typing: in the chatbox, in an "Enter amount" prompt, or in any
  focused text field.
- A key that matches a hotkey is **consumed**, so it does not also reach the game. That is what
  makes bare keys like `1` and `2` usable: an unconsumed digit would be typed into the chatbox, and
  hotkeys are then ignored because there is text in the chatbox. Keys that match nothing are left
  completely alone.

Per-state hotkeys and the cycle hotkeys work at the same time; leave the ones you do not want
unbound.

### If a hotkey does nothing at all

Another plugin has probably claimed the key. RuneLite stops dispatching a key press at the first
listener that consumes it, so whichever plugin registered earlier wins. 

### Dead keys

A dead key, such as the grave accent on an international layout, can be bound and works, but
Windows puts the keyboard into a pending-accent state before RuneLite ever sees the press. The
plugin swallows the accents so they do not reach the chatbox, yet it cannot undo that pending
state: after an odd number of presses, the next letter you type may come out accented.

If that bothers you, bind a key that produces no character, such as an F-key.

### Sharing a key with the game

*Pass hotkeys to the game* (off by default) stops the plugin consuming matched keys, so one key can
switch state **and** do its normal game action.

Only use it with keys that produce no text, such as F-keys or modifier combinations. An ordinary
letter or digit would go into the chatbox, and every following hotkey press would then be ignored
for as long as that text sits there.

## Configuration

Everything except the sets themselves lives in the normal plugin config:

| Setting | Default | Notes |
| --- | --- | --- |
| Size | 64 px | Applies to every state in every set. |
| Opacity | 100% | |
| Show background | off | A translucent panel and border behind the image. |
| Background colour | RuneLite's standard overlay brown | Only used while *Show background* is on. Supports transparency. |
| State name | Hidden | Draws the active state's name `Above the image` or `Below the image`. |
| Next state / Previous state / Hide overlay | unbound | The set-wide cycle hotkeys. |
| Pass hotkeys to the game | off | See *Sharing a key with the game* above. |

The sets themselves are stored as JSON under a hidden config key, because RuneLite's config panel
cannot express a dynamic list of sets each holding a dynamic list of states. That is why they are
edited in the side panel instead.

## Licence

BSD-2-Clause, see [LICENSE](LICENSE).
