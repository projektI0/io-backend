drop index product_ts;
alter table product drop column ts;
alter table product add column ts tsvector generated always as ( to_tsvector('english', product.description || product.name) ) stored;
create index product_ts ON product USING gin (ts);


