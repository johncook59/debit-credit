CREATE SEQUENCE IF NOT EXISTS public.hibernate_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.hibernate_sequence
    OWNER TO postgres;

CREATE SEQUENCE IF NOT EXISTS public.account_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.account_id_seq
    OWNER TO postgres;

CREATE TABLE IF NOT EXISTS public.account
(
    id integer NOT NULL DEFAULT nextval('account_id_seq'::regclass),
    balance numeric(19,2) NOT NULL,
    bid character varying(12) COLLATE pg_catalog."default" DEFAULT concat('A', lpad((nextval('hibernate_sequence'::regclass))::text, 8, '0'::text)),
    name character varying(20) COLLATE pg_catalog."default" NOT NULL,
    version integer NOT NULL,
    CONSTRAINT account_pkey PRIMARY KEY (id)
);

ALTER TABLE public.account
    OWNER to postgres;

CREATE INDEX IF NOT EXISTS idx_account_bid
    ON public.account USING btree
    (bid COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE SEQUENCE IF NOT EXISTS public.customer_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.customer_id_seq
    OWNER TO postgres;

CREATE TABLE IF NOT EXISTS public.customer
(
    id integer NOT NULL DEFAULT nextval('customer_id_seq'::regclass),
    bid character varying(12) COLLATE pg_catalog."default" DEFAULT concat('C', lpad((nextval('hibernate_sequence'::regclass))::text, 8, '0'::text)),
    email_address character varying(60) COLLATE pg_catalog."default" NOT NULL,
    given_name character varying(40) COLLATE pg_catalog."default" NOT NULL,
    password character varying(20) COLLATE pg_catalog."default" NOT NULL,
    surname character varying(40) COLLATE pg_catalog."default" NOT NULL,
    version integer NOT NULL,
    CONSTRAINT customer_pkey PRIMARY KEY (id),
    CONSTRAINT uk_email_address UNIQUE (email_address)
);

ALTER TABLE public.customer
    OWNER to postgres;

CREATE INDEX IF NOT EXISTS idx_customer_bid
    ON public.customer USING btree
    (bid COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS idx_customer_email_address
    ON public.customer USING btree
    (email_address COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS public.customer_account
(
    customer_id integer NOT NULL,
    account_id integer NOT NULL,
    CONSTRAINT fk_account_id_customer_account FOREIGN KEY (account_id)
        REFERENCES public.account (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_customer_id_customer_account FOREIGN KEY (customer_id)
        REFERENCES public.customer (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

ALTER TABLE public.customer_account
    OWNER to postgres;

CREATE SEQUENCE IF NOT EXISTS public.transaction_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.transaction_id_seq
    OWNER TO postgres;

CREATE TABLE IF NOT EXISTS public.transaction
(
    id integer NOT NULL DEFAULT nextval('transaction_id_seq'::regclass),
    account_bid character varying(255) COLLATE pg_catalog."default" NOT NULL,
    amount numeric(19,2) NOT NULL,
    balance numeric(19,2) NOT NULL,
    bid character varying(12) COLLATE pg_catalog."default" DEFAULT concat('T', lpad((nextval('hibernate_sequence'::regclass))::text, 10, '0'::text)),
    direction character varying(255) COLLATE pg_catalog."default" NOT NULL,
    processed_dt timestamp with time zone,
    user_bid character varying(12) COLLATE pg_catalog."default" NOT NULL,
    version integer NOT NULL,
    CONSTRAINT transaction_pkey PRIMARY KEY (id)
);

ALTER TABLE public.transaction
    OWNER to postgres;

CREATE INDEX IF NOT EXISTS idx_transaction_account_bid
    ON public.transaction USING btree
    (account_bid COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

