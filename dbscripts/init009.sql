alter table product
    add column IS_RECIPE bool;

update product
set IS_RECIPE = false
where IS_RECIPE is null;

alter table product
    alter column IS_RECIPE set not null;

create table RECIPE_INGREDIENT
(
    RECIPE_ID  bigint not null references product (id),
    PRODUCT_ID bigint not null references product (id),
    unique (RECIPE_ID, PRODUCT_ID)
);