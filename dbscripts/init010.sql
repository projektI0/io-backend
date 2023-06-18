create table BLACKLIST_SHOP
(
    SHOP_ID bigint not null references SHOP (ID),
    USER_ID bigint not null references LOGIN_USER (ID),
    unique (SHOP_ID, USER_ID)
)