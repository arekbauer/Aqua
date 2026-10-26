# Room database schema history

The exported JSON files in this directory are the versioned schema snapshots for `AquaDatabase`. Keep each generated version in source control and do not edit older snapshots.

When changing the database:

1. Increment `AquaDatabase.version` and build once to export the new schema.
2. Add a `Migration` from every supported prior version in `DatabaseMigrations.ALL` and register it through `DatabaseModule`.
3. Test the migration with Room's `MigrationTestHelper`, including preservation of existing intake and settings data.
4. Do not add destructive migration fallback; user history must survive app upgrades.
