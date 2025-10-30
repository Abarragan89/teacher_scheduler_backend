ALTER TABLE todos ADD COLUMN notification_sent BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE todos ADD COLUMN notification_sent_at TIMESTAMP NULL;
ALTER TABLE todos ADD COLUMN overdue_notification_sent BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_todos_notification_check ON todos(due_date, notification_sent, completed);