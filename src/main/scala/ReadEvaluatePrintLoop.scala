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

import scala.tools.nsc._
import interpreter._
import java.io._
import java.lang.{System => JSystem}

object ReadEvaluatePrintLoop { 
  //val helpOnReqT: String = "** Type ?? for help on reqT, type :h for help on Scala REPL"
  val helpOnReqT = "** Type :help for help on the Scala Read-Evaluate-Print-Loop"
  val versionMsg = s"\n** Welcome to reqT version $reqT_VERSION" +
    s"\n** Snapshot build: $BUILD_DATE" + 
    s"\n** Compiled with Scala version $SCALA_VERSION" +  
     "\n** Running Java version " + JSystem.getProperty("java.version") +  
     "\n** Running on " + JSystem.getProperty("java.vm.name")
  val startMsg = versionMsg +
     s"\n$PREAMBLE\n$helpOnReqT" +
    "\n** Starting reqT ..."
  
  @volatile
  var interpreter: Option[ReqTILoop] = None
  
  def reset() { 
  /*
    //the idea was that this should avoid memory leaks
    //https://issues.scala-lang.org/browse/SI-4331
    //but it does not seem to help...
    //test case:
      var j = 0
      while (true) {
        if (j % 1000 == 0) {
          $intp.interpret("""reqT.repl.reset""")
          $intp.bind("j",j)
          $intp.interpret(s"var i = $j")
        }
        $intp.interpret("""i += 1;println(i + ":" + new java.util.Date)""")
        j += 1
      }    
  */
    interpreter.map { i => 
      i.intp.reset
      i.initReqT
    }
  }
  
  class ReqTILoop(out : PrintWriter) extends ILoop(None, out) {
    override val prompt = "\nreqT> "
    override def loop() {
     if (isAsync) awaitInitialized()
     initReqT()
     super.loop()
    }
    def initReqT() {
     intp.quietBind("$intp", intp) //check if this is really needed??
     intp.interpret("reqT.initInterpreter($intp)")
    }
    override def helpCommand(line: String): Result = {
      if (line == "") echo(helpOnReqT)
      super.helpCommand(line)
    }

     override def printWelcome(): Unit = {
       out.println(startMsg)
       out.flush()
     }
  }
  
  class FileRunner(out : PrintWriter, fileName: String) extends ReqTILoop(out : PrintWriter) {
    override def loop() {
     if (isAsync) awaitInitialized() 
     intp.quietBind("$intp", intp) //check if this is really needed??
     intp.interpret("reqT.initInterpreter($intp)")
     intp.interpret(reqT.load(fileName))
    }
     override def printWelcome(): Unit = {
       out.println("** reqT FileRunner starting ...")
       out.flush()
     }
  }

  def startInterpreting() = {
    val out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(JSystem.out) ) )
    val settings = new GenericRunnerSettings(out.println)
    settings.usejavacp.value = true
    interpreter = Some(new ReqTILoop(out))
    interpreter.map(_.process(settings))
  }
  
  def interpretFile(fileName: String) = {
    val out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(JSystem.out) ) )
    val settings = new GenericRunnerSettings(out.println)
    settings.usejavacp.value = true
    interpreter = Some(new FileRunner(out, fileName))
    interpreter.map(_.process(settings))    
  }
  
}
