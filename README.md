# update-secondary-go-id-pipeline

Replaces secondary (obsolete) GO term accessions with their primary equivalents
in the `FULL_ANNOT` table.

## Logic

1. **Build secondary-to-primary map** — loads all `alt_id` synonyms from the three
   GO ontologies (BP, CC, MF) to create a mapping of secondary GO IDs to primary GO IDs.
2. **Delete conflicting annotations** — for each secondary annotation, checks if a matching
   primary annotation already exists (same object, reference, evidence, withInfo, xrefSource,
   qualifier, qualifier2, associatedWith). If so, deletes the secondary to avoid unique
   constraint violations.
3. **Update remaining annotations** — replaces `TERM_ACC` and `TERM` fields with the
   primary GO ID and term name.

## Logging

- `status` — pipeline progress and summary counts
- `pairs` — secondary-to-primary ID mappings processed
- `deleted` — conflicting annotations removed before update
- `updated` — annotations successfully updated to primary GO IDs

## Build and run

Requires Java 17. Built with Gradle:
```
./gradlew clean assembleDist
```
