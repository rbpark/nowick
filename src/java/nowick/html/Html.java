package nowick.html;

public class Html extends DomElement {
	private Head head;
	private Body body;
	
	public Html() {
		head = new Head();
		body = new Body();
		super.add(head);
		super.add(body);
	}
	
	protected void add(DomElement element) {
	}
	
	@Override
	public String getTag() {
		return "html";
	}
	
	public Head getHead() {
		return head;
	}
	
	public Body getBody() {
		return body;
	}
}
