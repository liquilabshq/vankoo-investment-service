-- An auction whose invoice the risk service finds not eligible is REJECTED, with
-- the reason it gave. Additive only: no existing row changes.
ALTER TABLE auctions DROP CONSTRAINT ck_auctions_status;

ALTER TABLE auctions ADD CONSTRAINT ck_auctions_status CHECK (
    status IN ('PENDING_VERIFICATION_RISK', 'DRAFT', 'PUBLISHED', 'FUNDING', 'FULLY_FUNDED', 'CLOSED', 'EXPIRED', 'CANCELLED', 'REJECTED')
);

ALTER TABLE auctions ADD (
    rejection_reason_code VARCHAR2(60 CHAR),
    rejection_reason VARCHAR2(500 CHAR),
    rejected_at TIMESTAMP(6) WITH TIME ZONE
);
