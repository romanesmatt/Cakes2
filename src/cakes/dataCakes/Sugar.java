package cakes.dataCakes;

@SuppressWarnings("serial")
public class Sugar extends Counter{
  volatile static int cTot=0;
  void incr() {cTot+=1;c=cTot;}
}