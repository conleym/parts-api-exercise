-- Create the parts database and application user.
-- For an actual deployed application, we'd want to keep the DDL permissions away from the application user for
-- security reasons. For local development, it is probably reasonable to skip it.
CREATE user parts PASSWORD 'parts';

CREATE DATABASE parts OWNER parts;
