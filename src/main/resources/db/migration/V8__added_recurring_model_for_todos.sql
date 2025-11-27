CREATE TABLE recurrence_pattern (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL, -- 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'

    -- Weekly fields
    days_of_week VARCHAR(20), -- 1, 3, 5 for Mon/Wed/Fri

    -- Monthly fields
    month_pattern_type VARCHAR(20), -- 'BY_DATE' OR 'BY_DAY'
    days_of_month VARCHAR(50), -- '1, 15, -1' for 1st, 15th, last day
    nth_weekday_occurrence INT, -- 1, 2, 3, -1
    nth_weekday_day INT, -- 0-6 (Sunday=0, for "2nd Tuesday")

    -- Yearly fields
    yearly_month INT, -- 1-12 for which month
    yearly_day INT, -- 1-31 for which day of that month

    -- Common fields
    time_of_day TIME NOT NULL, --When to trigger each day
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

ALTER TABLE todos ADD COLUMN is_recurring BOOLEAN DEFAULT FALSE;
ALTER TABLE todos ADD COLUMN recurrence_pattern_id UUID REFERENCES recurrence_pattern(id);
ALTER TABLE todos ADD COLUMN next_occurrence TIMESTAMP;
ALTER TABLE todos ADD COLUMN last_generated_date DATE;
