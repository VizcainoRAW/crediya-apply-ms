-- Loan Service Database Schema
-- Database: loan_service
-- Schema: public

-- Create loan_types table
CREATE TABLE loan_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    anual_nominal_rate DECIMAL(6,4) NOT NULL, -- Allows rates like 15.2500%
    auto_validation BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create loan_applications table
CREATE TABLE loan_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    loan_type_id UUID NOT NULL,
    amount DECIMAL(15,2) NOT NULL, -- Allows amounts up to 999,999,999,999.99
    term_months INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraint
    CONSTRAINT fk_loan_applications_loan_type
        FOREIGN KEY (loan_type_id) REFERENCES loan_types(id)
);

-- Create indexes for better performance
CREATE INDEX idx_loan_applications_user_id ON loan_applications(user_id);
CREATE INDEX idx_loan_applications_loan_type_id ON loan_applications(loan_type_id);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);
CREATE INDEX idx_loan_applications_created_at ON loan_applications(created_at);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_loan_types_updated_at
    BEFORE UPDATE ON loan_types
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_loan_applications_updated_at
    BEFORE UPDATE ON loan_applications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample data
INSERT INTO loan_types (name, anual_nominal_rate, auto_validation) VALUES
    ('Personal Loan', 12.50, false),
    ('Car Loan', 8.75, true),
    ('Home Loan', 6.25, false),
    ('Student Loan', 4.50, true);