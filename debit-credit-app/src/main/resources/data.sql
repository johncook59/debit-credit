INSERT INTO customer(
	email_address,
    given_name,
    password,
    surname,
    version)
    SELECT 'admin@zarg.co.uk',
	'admin',
	'letmein',
	'admin',
	0
WHERE NOT EXISTS (
    SELECT 1 FROM customer WHERE email_address='admin@zarg.co.uk'
);