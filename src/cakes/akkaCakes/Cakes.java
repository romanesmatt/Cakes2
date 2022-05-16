package cakes.akkaCakes;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.pattern.Patterns;
import cakes.akkaUtils.AkkaConfig;
import cakes.dataCakes.Cake;
import cakes.dataCakes.Gift;
import cakes.dataCakes.Sugar;
import cakes.dataCakes.Wheat;

@SuppressWarnings("serial")
class GiftRequest implements Serializable {
}
//--------

/**
 * Producer class as outlined for Task 2.
 *
 * @param <T>
 */
abstract class Producer<T> extends AbstractActor {
    public abstract CompletableFuture<T> make();
}


class Alice extends Producer<Wheat> {
    Queue<Wheat> products = new ArrayDeque<>();

    int maxSize;
    boolean running = true;

    public Alice(int maxSize) {
        this.maxSize = maxSize;
    }

    public Receive createReceive() {
        return receiveBuilder()
                //.match(recievedObjectType, doThis).build();

                .match(Wheat.class, T -> { //T
                    products.add(T);
                })
                .match(String.class, s -> s.equalsIgnoreCase("makeone"), s -> { //MakeOne
                    if (products.size() >= maxSize) { //list is full
                        running = false;
                    } else { //list is not full
                        ActorRef me = self();
                        CompletableFuture<Wheat> futureProduct = make();
                        futureProduct.thenAcceptAsync(wheat -> {
                            me.tell(wheat, self());
                            self().tell("makeOne", self());
                        });
//					pipe(futureProduct, context().dispatcher()).to(self());
                    }
                })
                .match(String.class, s -> s.equalsIgnoreCase("giveone"), s -> { //GiveOne
                    ActorRef sender = sender();

                    if (products.isEmpty()) { //"sorry, the list is empty, I'll make one and send it to you in the FUTURE"
                        make().thenAcceptAsync(wheat -> sender.tell(wheat, self()));
                    } else {
                        if (!running && !products.isEmpty()) {
                            running = true;
                            self().tell("makeOne", self());
                        }
                        sender.tell(products.poll(), self()); //Here's the wheat
                    }
                })
                .build();
    }

    @Override
    public CompletableFuture<Wheat> make() {
        return CompletableFuture.supplyAsync(() -> {
            return new Wheat();
        });
    }
}

class Bob extends Producer<Sugar> {
    Queue<Sugar> products = new ArrayDeque<>();

    int maxSize;
    boolean running = true;

    public Bob(int maxSize) {
        this.maxSize = maxSize;
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(Sugar.class, T -> { //T
                    products.add(T);
                })
                .match(String.class, s -> s.equalsIgnoreCase("makeone"), s -> { //MakeOne
                    if (products.size() >= maxSize) { //list is full
                        running = false;
                    } else { //list is not full
                        ActorRef me = self();
                        CompletableFuture<Sugar> futureProduct = make();
                        futureProduct.thenAcceptAsync(sugar -> {
                            me.tell(sugar, self());
                            self().tell("makeOne", self());
                        });
                        //pipe(futureProduct, context().dispatcher()).to(self());
                    }
                })
                .match(String.class, s -> s.equalsIgnoreCase("giveone"), s -> { //GiveOne
                    ActorRef sender = sender();
                    if (products.isEmpty()) { //"sorry, the list is empty, I'll make one and send it to you in the FUTURE"
                        make().thenAcceptAsync(sugar -> sender.tell(sugar, self()));
                    } else {
                        if (!running && !products.isEmpty()) {
                            running = true;
                            self().tell("makeOne", self());
                        }
                        sender.tell(products.poll(), self()); //Here's the sugar
                    }
                })
                .build();
    }

    @Override
    public CompletableFuture<Sugar> make() {
        return CompletableFuture.supplyAsync(() -> {
            return new Sugar();
        });
    }
}

class Charles extends Producer<Cake> {
    Queue<Wheat> ws = new ArrayDeque<>();//no synchronization issues!
    Queue<Sugar> ss = new ArrayDeque<>();//no synchronization issues!

    Queue<Cake> products = new ArrayDeque<>();

    int maxSize;

    ActorRef alice;
    ActorRef bobOne;
    ActorRef bobTwo;
    ActorRef bobThree;
    ActorRef bobFour;

    boolean fourBobs = false;

    boolean running = true;

    public Charles(int maxSize, ActorRef alice, ActorRef bobOne) {
        this.maxSize = maxSize;
        this.alice = alice;
        this.bobOne = bobOne;
    }

    public Charles(int maxSize, ActorRef alice, ActorRef bobOne, ActorRef bobTwo, ActorRef bobThree, ActorRef bobFour) {
        this.maxSize = maxSize;
        this.alice = alice;
        this.bobOne = bobOne;
        this.bobTwo = bobTwo;
        this.bobThree = bobThree;
        this.bobFour = bobFour;

        fourBobs = true;
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(Wheat.class, T -> { //T
                    ws.add(T);
                })
                .match(Sugar.class, T -> { //T
                    ss.add(T);
                })
                .match(Cake.class, T -> { //T
                    products.add(T);
                })
                .match(String.class, s -> s.equalsIgnoreCase("makeone"), s -> { //MakeOne
                    //first, get ingredients
                    CompletableFuture<?> getWheat = Patterns.ask(alice, "giveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

                    CompletableFuture<?> getSugar = Patterns.ask(bobOne, "giveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

                    if (fourBobs) {
                        CompletableFuture<?> getSugarTwo = Patterns.ask(bobTwo, "giveOne",
                                Duration.ofMillis(100000)).toCompletableFuture();
                        CompletableFuture<?> getSugarThree = Patterns.ask(bobThree, "giveOne",
                                Duration.ofMillis(100000)).toCompletableFuture();
                        CompletableFuture<?> getSugarFour = Patterns.ask(bobFour, "giveOne",
                                Duration.ofMillis(100000)).toCompletableFuture();
                    }

                    //Second, make cakes
                    if (products.size() >= maxSize) { //list is full
                        running = false;
                    } else { //list is not full
                        ActorRef me = self();
                        CompletableFuture<Cake> futureProduct = make();
                        futureProduct.thenAcceptAsync(cake -> {
                            me.tell(cake, self());
                            self().tell("makeOne", self());
                        });
                        //pipe(futureProduct, context().dispatcher()).to(self());
                    }
                })
                .match(String.class, s -> s.equalsIgnoreCase("giveone"), s -> { //GiveOne
                    ActorRef sender = sender();
                    if (products.isEmpty()) { //"sorry, the list is empty, I'll make one and send it to you in the FUTURE"
                        make().thenAcceptAsync(cake -> sender.tell(cake, self()));
                    } else {
                        if (!running && !products.isEmpty()) { //I
                            running = true;
                            self().tell("makeOne", self());
                        }
                        sender.tell(products.poll(), self()); //Here's the cake
                    }
                }).build();
    }

    @Override
    public CompletableFuture<Cake> make() {
        return CompletableFuture.supplyAsync(() -> {
            return new Cake(ss.poll(), ws.poll());
        });
    }
}

class Tim extends AbstractActor {
    volatile int hunger;
    boolean running = true;
    ActorRef cakeMan;
    ActorRef originalSender;

    public Tim(int hunger, ActorRef cakeMan) {
        this.hunger = hunger;
        this.cakeMan = cakeMan;
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(GiftRequest.class, () -> running, gr -> {
                    //Main thread who wants return
                    originalSender = sender();

                    CompletableFuture<?> getCake = Patterns.ask(cakeMan, "giveOne",
                            Duration.ofMillis(100000)).toCompletableFuture();

                    CompletableFuture<String> eatCake =
                            CompletableFuture.allOf(getCake)
                                    .thenApplyAsync(v -> {
                                        self().tell((Cake) getCake.join(), cakeMan);

                                        return "WOOOOOO! but I'm still hungry " + hunger;
                                    });
                })
                .match(Cake.class, () -> running, c -> {
                    hunger -= 1;
                    System.out.println("WOOOOOO! but I'm still hungry " + hunger);

                    if (hunger > 0) {
                        self().tell(new GiftRequest(), originalSender);
                        return;
                    }
                    running = false;
                    originalSender.tell(new Gift(), self());
                }).build();
    }
}

public class Cakes {
    public static void main(String[] args) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        Gift g = computeGift(1000);
        assert g != null;
        System.out.println(
                "\n\n-----------------------------\n\n" +
                        g +
                        "\n\n-----------------------------\n\n");
    }

    public static Gift computeGift(int hunger) {
        ActorSystem s = AkkaConfig.newSystem("Cakes", 2501, Map.of(
                "Tim", "192.168.56.1",
                "Bob", "192.168.56.1",
                "Charles", "192.168.56.1"
                //Alice stays local
        ));

        //=====MAKE CAKE=====//
        ActorRef bobOne = s.actorOf(Props.create(Bob.class, () -> new Bob(1000)), "BobOne"); //makes sugar
        ActorRef bobTwo = s.actorOf(Props.create(Bob.class, () -> new Bob(1000)), "BobTwo"); //makes sugar
        ActorRef bobThree = s.actorOf(Props.create(Bob.class, () -> new Bob(1000)), "BobThree"); //makes sugar
        ActorRef bobFour = s.actorOf(Props.create(Bob.class, () -> new Bob(1000)), "BobFour"); //makes sugar

        ActorRef alice = s.actorOf(Props.create(Alice.class, () -> new Alice(1000)), "Alice"); //makes wheat

        //makes cakes with wheat and sugar
        ActorRef charles = s.actorOf(Props.create(Charles.class, () -> new Charles(1000, bobOne, alice)), "Charles");

        //Charles with the 4 bobs
        //ActorRef charles = s.actorOf(Props.create(Charles.class,()->new Charles(1000, bobOne, alice, bobTwo, bobThree, bobFour)),"Charles");

        //tim wants to eat cakes
        ActorRef tim = s.actorOf(Props.create(Tim.class, () -> new Tim(hunger, charles)), "Tim");

        //Begin!
        long initialTimeStamp = System.currentTimeMillis();
//		alice.tell(charles,tim);
//		bobOne.tell(charles,tim);

        CompletableFuture<Object> gift = Patterns.ask(tim, new GiftRequest(), Duration.ofMillis(100000000)).toCompletableFuture();

        try {
            //wait until the "gift" task is complete...
            return (Gift) gift.join();
        }
        //...then...
        finally {
            System.out.println("Result: " + (System.currentTimeMillis() - initialTimeStamp));
            //...Murder them all with a PoisonPill, sent by no one.
            alice.tell(PoisonPill.getInstance(), ActorRef.noSender());
            bobOne.tell(PoisonPill.getInstance(), ActorRef.noSender());
            bobTwo.tell(PoisonPill.getInstance(), ActorRef.noSender());
            bobThree.tell(PoisonPill.getInstance(), ActorRef.noSender());
            bobFour.tell(PoisonPill.getInstance(), ActorRef.noSender());
            charles.tell(PoisonPill.getInstance(), ActorRef.noSender());
            tim.tell(PoisonPill.getInstance(), ActorRef.noSender());
            s.terminate();
        }
    }
}