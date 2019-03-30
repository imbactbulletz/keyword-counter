import job.Job;

public class JobDispatcher extends Thread {


    @Override
    public void run() {
        System.out.println("> Job Dispatcher started.");
        while(true) {
            try {
                Job job = Main.jobQueue.take();

                // got poisoned
                if(job.getQuery().equals(Messages.POSION_MESSAGE)) {
                    System.out.println("> Job Dispatcher shutting down.");
                    // todo poison file scanner and web scanner
                    return;
                }

                System.out.println("Recieved a Job. (" + job.getType() + "," + job.getQuery() + ")");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
