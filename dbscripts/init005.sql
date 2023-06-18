alter table PRODUCT_TAG add column MAIN_TAG_ID int not null;

alter table PRODUCT_TAG add constraint PRODUCT_TAG_MAIN_TAG_ID_FK foreign key (MAIN_TAG_ID) references TAG(ID);

alter table TAG add constraint PARENT_TAG_ID_TAG_ID_FK foreign key (PARENT_TAG_ID) references TAG(ID);

INSERT INTO TAG (NAME) VALUES
    ('dairy'),
    ('meat'),
    ('baked goods'),
    ('vegan'),
    ('other');

INSERT INTO TAG (NAME, PARENT_TAG_ID) VALUES
    ('eggs', 1),
    ('bread', 3),
    ('fish', 2),
    ('chicken', 2),
    ('tofu', 4);
