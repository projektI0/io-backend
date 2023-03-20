create table LOGIN_USER (
    ID serial primary key,
    LOGIN varchar not null,
    PASSWORD varchar not null
);

create table ROLE (
    ID int not null primary key,
    NAME varchar not null
);

insert into ROLE (ID, NAME) values (1, 'ADMIN');
insert into ROLE (ID, NAME) values (2, 'USER');


create table LOGIN_USER_ROLE (
    LOGIN_USER_ID int not null,
    ROLE_ID int not null
);

alter table LOGIN_USER_ROLE add constraint LOGIN_USER_ROLE_LOGIN_USER_ID_FK foreign key (LOGIN_USER_ID) references LOGIN_USER(ID);
alter table LOGIN_USER_ROLE add constraint LOGIN_USER_ROLE_ROLE_ID_FK foreign key (ROLE_ID) references ROLE(ID);

create table TAG (
    ID serial primary key,
    NAME varchar not null,
    PARENT_TAG_ID int default null
);


create table PRODUCT (
    ID serial primary key,
    NAME varchar not null,
    DESCRIPTION varchar not null
);

create table PRODUCT_TAG (
    PRODUCT_ID int not null,
    TAG_ID int not null
);

alter table PRODUCT_TAG add constraint PRODUCT_TAG_PRODUCT_ID_FK foreign key (PRODUCT_ID) references PRODUCT(ID);
alter table PRODUCT_TAG add constraint PRODUCT_TAG_TAG_ID_FK foreign key (TAG_ID) references TAG(ID);

create table SHOP (
    ID serial primary key,
    NAME varchar not null,
    LONGITUDE double precision not null,
    LATITUDE double precision not null,
    ADDRESS varchar not null
);

create table SHOP_TAG (
    SHOP_ID int not null,
    TAG_ID int not null
);

alter table SHOP_TAG add constraint SHOP_TAG_SHOP_ID_FK foreign key (SHOP_ID) references SHOP(ID);
alter table SHOP_TAG add constraint SHOP_TAG_TAG_ID_FK foreign key (TAG_ID) references TAG(ID);

create table SHOPPING_LIST (
    ID serial primary key,
    NAME varchar,
    OWNER_ID int not null,
    CREATED_AT timestamp default now()
);

create table SHOPPING_LIST_PRODUCT (
    SHOPPING_LIST_ID int not null,
    PRODUCT_ID int not null,
    QUANTITY int not null
);

alter table SHOPPING_LIST_PRODUCT add constraint SHOPPING_LIST_PRODUCT_SHOPPING_LIST_ID_FK foreign key (SHOPPING_LIST_ID) references SHOPPING_LIST(ID);
alter table SHOPPING_LIST_PRODUCT add constraint SHOPPING_LIST_PRODUCT_PRODUCT_ID_FK foreign key (PRODUCT_ID) references PRODUCT(ID);
create unique index SHOPPING_LIST_PRODUCT_SHOPPING_LIST_ID_PRODUCT_ID_UK on SHOPPING_LIST_PRODUCT (SHOPPING_LIST_ID, PRODUCT_ID);

create table SHOPPING_LIST_SHOP (
    SHOPPING_LIST_ID int not null,
    SHOP_ID int not null,
    STOP_ID int not null
);

alter table SHOPPING_LIST_SHOP add constraint SHOPPING_LIST_SHOP_SHOPPING_LIST_ID_FK foreign key (SHOPPING_LIST_ID) references SHOPPING_LIST(ID);
alter table SHOPPING_LIST_SHOP add constraint SHOPPING_LIST_SHOP_SHOP_ID_FK foreign key (SHOP_ID) references SHOP(ID);

create unique index SHOPPING_LIST_SHOP_SHOPPING_LIST_ID_SHOP_ID_UK on SHOPPING_LIST_SHOP (SHOPPING_LIST_ID, SHOP_ID);
create unique index SHOPPING_LIST_SHOP_SHOPPING_LIST_ID_STOP_ID_UK on SHOPPING_LIST_SHOP (SHOPPING_LIST_ID, STOP_ID);