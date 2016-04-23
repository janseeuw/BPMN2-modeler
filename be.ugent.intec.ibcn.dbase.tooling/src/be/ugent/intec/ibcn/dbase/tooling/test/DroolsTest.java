package be.ugent.intec.ibcn.dbase.tooling.test;

import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

/**
 * This is a sample class to launch a rule.
 */
public class DroolsTest {

    public static final void main(String[] args) {
        try {
        	KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
           
            ksession.insert(new Message("Hello World")); 
             
            ksession.fireAllRules();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static KnowledgeBase readKnowledgeBase() {
    	KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

    	kbuilder.add(ResourceFactory.newClassPathResource("Sample.dsl"), ResourceType.DSL);
    	kbuilder.add(ResourceFactory.newClassPathResource("Sample.dslr"), ResourceType.DSLR);

        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors()
            .toString());
        }
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;

	}

	public static class Message {

        public static final int HELLO = 0;
        public static final int GOODBYE = 1;

        private String message;

        private int status;

        public Message(String message) {
			this.message = message;
		}

		public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatus() {
            return this.status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

    }

}
