package no.course.company.minicontainer.demo;

import no.course.company.minicontainer.MiniContainer;

public class MiniApp {
    public static void main(String[] args) throws Exception {
        MiniContainer ctx = new MiniContainer("no.course.company.minicontainer.demo");
        ctx.get(CompanyService.class).register("Acme Corp");
        System.out.println("Beans: " + ctx.beanTypes());
    }
}
