package nowick.html;

public class Html extends DomElement {
	public Html() {
		Head head = new Head();
		Body body = new Body();
		super.add(head);
		super.add(body);
	}
	
	protected void add(DomElement element) {
	}
	
	@Override
	public String getTag() {
		return "html";
	}
}
