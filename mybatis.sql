ALTER TABLE tb_employee ADD CONSTRAINT fk_emp_dept
FOREIGN KEY (d_id) REFERENCES tb_dept(id)