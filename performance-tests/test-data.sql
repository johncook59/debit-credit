DROP TABLE IF EXISTS public.all_requests;

WITH cust_accs AS (
	SELECT c.bid AS customer_bid,
			a.bid AS account_bid,
			floor(random() * 100) AS score
	FROM public.customer c
	JOIN public.customer_account ca ON ca.customer_id = c.id
	join public.account a ON a.id = ca.account_id
	LIMIT 10000
)
SELECT customer_bid,
		account_bid,
		score
INTO public.all_requests
FROM cust_accs
ORDER BY random();

\COPY (SELECT customer_bid,account_bid FROM public.all_requests WHERE score != 0 ORDER BY random()) TO './src/test/resources/other_requests.csv' DELIMITER ',' CSV HEADER;
\COPY (SELECT customer_bid,account_bid FROM public.all_requests WHERE score = 0 ORDER BY random()) TO './src/test/resources/top_requests.csv' DELIMITER ',' CSV HEADER;
