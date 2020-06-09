-- 
-- El contenido de este fichero se cargará al arrancar la aplicación, suponiendo que uses
-- 		application-default ó application-externaldb en modo 'create'
--

-- Usuario de ejemplo con username = b y contraseña = aa  
INSERT INTO user(id,enabled,username,password,roles,first_name,last_name) VALUES (
	1, 1, 'a', 
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u',
	'USER,ADMIN',
	'Abundio', 'Ejémplez'
);

-- Otro usuario de ejemplo con username = b y contraseña = aa  
INSERT INTO user(id,enabled,username,password,roles,first_name,last_name) VALUES (
	2, 1, 'b', 
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u',
	'USER,PSICOLOGO',
	'Berta', 'Muéstrez'
);

-- Otro usuario de ejemplo con username = c y contraseña = aa  
INSERT INTO user(id,enabled,username,password,mail,roles,first_name,last_name,psychologist_id) VALUES (
	3, 1, 'c', 
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u','albertuko@gmail.com',
	'USER,PACIENTE',
	'Alberto', 'López',2
);

-- Otro usuario de ejemplo con username = d y contraseña = aa  
INSERT INTO user(id,enabled,username,password,mail,roles,first_name,last_name,psychologist_id) VALUES (
	4, 1, 'd', 
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u','manolakbsa@gmail.com',
	'USER,PACIENTE',
	'Manola', 'Cabezabola',2
);
-- Unos pocos auto-mensajes de prueba
INSERT INTO MESSAGE VALUES(1,NULL,'2020-03-23 10:48:11.074000','probando 1',1,1);
INSERT INTO MESSAGE VALUES(2,NULL,'2020-03-23 10:48:15.149000','probando 2',1,1);
INSERT INTO MESSAGE VALUES(3,NULL,'2020-03-23 10:48:18.005000','probando 3',1,1);
INSERT INTO MESSAGE VALUES(4,NULL,'2020-03-23 10:48:20.971000','probando 4',1,1);
INSERT INTO MESSAGE VALUES(5,NULL,'2020-03-23 10:48:22.926000','probando 5',1,1);
INSERT INTO MESSAGE VALUES(6,NULL,'2020-03-23 10:48:25.729000','probando 6',1,1);
INSERT INTO MESSAGE VALUES(7,NULL,'2020-03-23 10:48:30.522000','probando 7',1,1);
INSERT INTO MESSAGE VALUES(8,NULL,'2020-03-23 10:48:32.332000','probando 8',1,1);
INSERT INTO MESSAGE VALUES(9,NULL,'2020-03-23 10:48:35.531000','probando 9',1,1);
INSERT INTO MESSAGE VALUES(10,NULL,'2020-03-23 10:48:39.911000','probando 10',1,1);
