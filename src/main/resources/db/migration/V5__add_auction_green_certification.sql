-- An auction is green when the risk service validated its issuer's environmental
-- certificate. The certificate and the criteria it passed are kept (US10). Additive
-- only: no existing row changes, and an auction without a certificate is not green.
ALTER TABLE auctions ADD (
    green_certification_id VARCHAR2(100 CHAR),
    green_standard VARCHAR2(20 CHAR),
    green_issuer VARCHAR2(200 CHAR),
    green_valid_until DATE,
    CONSTRAINT ck_auctions_green_standard CHECK (green_standard IS NULL OR green_standard IN ('ISO_14001', 'ISO_50001'))
);

CREATE TABLE auction_green_criteria (
    auction_id VARCHAR2(36 CHAR) NOT NULL,
    criterion_code VARCHAR2(40 CHAR) NOT NULL,
    detail VARCHAR2(300 CHAR),
    CONSTRAINT pk_auction_green_criteria PRIMARY KEY (auction_id, criterion_code),
    CONSTRAINT fk_auction_green_criteria_auction FOREIGN KEY (auction_id) REFERENCES auctions (id)
);
