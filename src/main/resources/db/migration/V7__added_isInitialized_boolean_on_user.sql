ALTER TABLE users
ADD COLUMN is_initialized BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX uq_user_listname
ON todo_lists (user_id, LOWER(list_name));

CREATE OR REPLACE FUNCTION prevent_unlisted_delete()
RETURNS trigger AS $$
BEGIN
    IF OLD.name = 'unlisted' THEN
        RAISE EXCEPTION 'Cannot delete the unlisted todo list';
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_unlisted_delete
BEFORE DELETE ON todo_lists
FOR EACH ROW
EXECUTE FUNCTION prevent_unlisted_delete();
