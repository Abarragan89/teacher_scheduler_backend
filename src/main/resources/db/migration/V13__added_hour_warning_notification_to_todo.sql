ALTER TABLE todos DROP COLUMN overdue_notification_sent;
ALTER TABLE todos ADD COLUMN hour_warning_notification_sent BOOLEAN NOT NULL DEFAULT FALSE;
