
CREATE  TABLE tb_temp(
  id INT (11) PRIMARY KEY auto_increment,
  dept_name VARCHAR(255)
);

ALTER TABLE tb_employee ADD COLUMN d_id INT(11);

ALTER TABLE tb_employee ADD CONSTRAINT fk_emp_dept
FOREIGN KEY (d_id) REFERENCES tb_dept(id)