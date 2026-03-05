# update-secondary-go-id-pipeline
Replaces secondary (obsolete) GO term accessions with their primary equivalents in the FULL_ANNOT table.
Loads all `alt_id` synonyms from the three GO ontologies (BP, CC, MF) to build a secondary-to-primary mapping, then updates each affected annotation's `TERM_ACC` and `TERM` fields.
Before updating, deletes any annotation that would become a duplicate (matching object, reference, evidence, withInfo, xrefSource, qualifier, qualifier2, and associatedWith) of an existing primary annotation to avoid unique constraint violations.
