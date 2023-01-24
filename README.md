# update-secondary-go-id-pipeline
For GO annotations, replace secondary GO IDs with primary GO IDs.

LOGIC

- Load all 'alt_id' synonyms for GO terms. These are the secondary GO IDs.
- Update GO annotations replacing secondary ids with primary ids.
- If the update would have created a duplicate annotation, delete it instead.
