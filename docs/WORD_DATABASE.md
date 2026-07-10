# Word Database

Words are bundled in `WordBank.kt` as local structured data and converted to `WordEntry` records.

Each entry includes:

- `id`
- `displayName`
- `category`
- `difficulty`
- `aliases`
- `recognitionLabels`

Current database targets:

- 150+ easy words
- 150+ medium words
- 100+ hard words

Pairing rules avoid used words in the current match and prefer prompts from different categories.
