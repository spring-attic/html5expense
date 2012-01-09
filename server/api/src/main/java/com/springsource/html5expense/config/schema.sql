alter table EXPENSE drop foreign key FKDCC054382876AD53;
drop table if exists ELIGIBLE_CHARGE;
drop table if exists EXPENSE;
drop table if exists EXPENSE_REPORT;
create table ELIGIBLE_CHARGE (id bigint not null auto_increment, amount decimal(19,2), category varchar(255), date datetime, merchant varchar(255), primary key (id)) ENGINE=InnoDB;
create table EXPENSE (id integer not null auto_increment, amount decimal(19,2), category varchar(255), chargeId bigint, date datetime, flag varchar(255), merchant varchar(255), receipt varchar(255), receiptExtension varchar(255), expenseReport_id bigint, primary key (id)) ENGINE=InnoDB;
create table EXPENSE_REPORT (id bigint not null auto_increment, purpose varchar(255), receiptRequiredAmount decimal(19,2), state varchar(255), primary key (id)) ENGINE=InnoDB;
alter table EXPENSE add index FKDCC054382876AD53 (expenseReport_id), add constraint FKDCC054382876AD53 foreign key (expenseReport_id) references EXPENSE_REPORT (id);