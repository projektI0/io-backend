alter table product add column ts tsvector generated always as ( to_tsvector('english', product.description) ) stored;
create index product_ts ON product USING gin (ts);
