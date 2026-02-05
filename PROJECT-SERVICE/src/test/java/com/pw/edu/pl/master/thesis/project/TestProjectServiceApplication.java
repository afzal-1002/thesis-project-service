package com.pw.edu.pl.master.thesis.project;

import org.springframework.boot.SpringApplication;

public class TestProjectServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(ProjectServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
