package CustomAnnotations;

//@MethodInfo(purpose = "Test") // '@MethodInfo' not applicable to type
public class Main {
  @MethodInfo(author = "Julia", purpose = "Print Hello World")
  public void printHelloWorld () {
    System.out.println("Hello World");
  }
}
