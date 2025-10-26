--liquibase formatted sql

--changeset anesterov:001-create-notification-task-table
CREATE TABLE notification_task (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    notification_date_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

--changeset anesterov:001-create-notification-task-table-indexes
CREATE INDEX idx_notification_task_datetime ON notification_task(notification_date_time);
CREATE INDEX idx_notification_task_chat_id ON notification_task(chat_id);
CREATE INDEX idx_notification_task_status ON notification_task(status);
