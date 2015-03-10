

ALTER TABLE PARTY_STRUCT_RULE DROP CONSTRAINT FK_PARTY_STRUCT_RULE_TYPE;
ALTER TABLE PARTY_STRUCT_RULE DROP CONSTRAINT FK_PARTY_STRUCT_RULE_TYPE_PARENT;
ALTER TABLE PARTY_STRUCT_RULE DROP CONSTRAINT FK_PARTY_STRUCT_RULE_TYPE_CHILD;
ALTER TABLE PARTY_STRUCT_RULE DROP CONSTRAINT PK_PARTY_STRUCT_RULE;

ALTER TABLE PARTY_STRUCT_RULE DROP CONSTRAINT FK_PARTY_STRUCT_RULE_DIM;
ALTER TABLE PARTY_STRUCT_RULE DROP COLUMN DIM_ID;
ALTER TABLE PARTY_STRUCT_RULE ALTER COLUMN PARENT_TYPE_ID SET NULL;
ALTER TABLE PARTY_STRUCT_RULE ADD COLUMN ID BIGINT;
ALTER TABLE PARTY_STRUCT_RULE ADD CONSTRAINT PK_PARTY_STRUCT_RULE PRIMARY KEY(ID);
ALTER TABLE PARTY_STRUCT_RULE ALTER COLUMN ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1);

ALTER TABLE PARTY_STRUCT_RULE ADD CONSTRAINT FK_PARTY_STRUCT_RULE_TYPE FOREIGN KEY(STRUCT_TYPE_ID) REFERENCES PARTY_STRUCT_TYPE(ID);
ALTER TABLE PARTY_STRUCT_RULE ADD CONSTRAINT FK_PARTY_STRUCT_RULE_TYPE_PARENT FOREIGN KEY(PARENT_TYPE_ID) REFERENCES PARTY_TYPE(ID);
ALTER TABLE PARTY_STRUCT_RULE ADD CONSTRAINT FK_PARTY_STRUCT_RULE_TYPE_CHILD FOREIGN KEY(CHILD_TYPE_ID) REFERENCES PARTY_TYPE(ID);
