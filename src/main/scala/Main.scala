/***     
**                  _______        
**                 |__   __|   reqT - a requriements engineering tool  
**   _ __  ___   __ _ | |      (c) 2011-2014, Lund University  
**  |  __|/ _ \ / _  || |      http://reqT.org
**  | |  |  __/| (_| || |   
**  |_|   \___| \__  ||_|   
**                 | |      
**                 |_|      
** reqT is open source, licensed under the BSD 2-clause license: 
** http://opensource.org/licenses/bsd-license.php 
**************************************************************************/

package reqT

object Main {
  def help() {
    println("reqT - a requriements engineering tool, visit http://reqT.org")
    println("<no arg>     Start reqT inside the Scala Read-Evaluate-Print-Loop")
    println("--help       -h   Print this message")
    println("--meta       -m   Generate metamodel to file GENERATED-metamodel.scala")
    println("--interpret  -i   <file> Interpret file")
  }
  def genmeta() {
        println("Generating metamodel...")
        reqT.metaprog.makeMetamodel().save("GENERATED-metamodel.scala")
        println("DONE!")
        println("""To replace old execute> cp GENERATED-metamodel.scala src/main/scala/.""")
        println("""To compile new execute> scalac -cp "lib\*" -d target src/main/scala/*""")
  }
  def interpret(fileNameOpt: Option[String]) {
    println("reqT --interpret " + fileNameOpt.getOrElse("ERROR no file; usage reqT -i <filename>"))
    fileNameOpt.map(ReadEvaluatePrintLoop.interpretFile(_)).collect { 
      case Some(true) => println("** DONE!")
      case _ => println("FAILED!")
    }
  }
  def main(args : Array[String]) : Unit =  {
    if (args.size == 0) ReadEvaluatePrintLoop.startInterpreting
    else args(0) match {
      case arg0 if Set("--hello", "--help", "-h")(arg0) => help()
      case arg0 if Set("--meta", "-m")(arg0)            => genmeta()
      case arg0 if Set("--interpret", "-i")(arg0)       => interpret(args.drop(1).headOption)
      case arg0 => println("Unknown args: " + args.mkString(" "))
    }
  }
  
}
