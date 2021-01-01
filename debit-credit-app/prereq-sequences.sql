CREATE SEQUENCE IF NOT EXISTS customer_bid_seq;
ALTER SEQUENCE customer_bid_seq
    OWNER TO postgres;

CREATE SEQUENCE IF NOT EXISTS account_bid_seq;
ALTER SEQUENCE account_bid_seq
    OWNER TO postgres;

CREATE SEQUENCE IF NOT EXISTS public.transaction_bid_seq;
ALTER SEQUENCE public.transaction_bid_seq
    OWNER TO postgres;
