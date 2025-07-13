package me.chrommob.baritoneremover.data;

import me.chrommob.baritoneremover.BaritoneRemover;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class Scheduler {
    private BaritoneRemover plugin;
    private static final boolean isFolia;
    private static final MethodHandle execute;
    private static final MethodHandle runAtFixedRate;
    private final Map<Integer, Object> tasks = new HashMap<>();
    private static final Object globalRegionScheduler;
    public Scheduler(BaritoneRemover plugin) {
        this.plugin = plugin;
    }

    static {
        MethodHandle execute1 = null;
        MethodHandle runAtFixedRate1 = null;
        Object globalRegionScheduler1 = null;
        boolean isFolia1;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia1 = true;
        } catch (ClassNotFoundException e) {
            isFolia1 = false;
        }
        isFolia = isFolia1;
        if (isFolia) {
            Class<? extends Server> serverClass = Bukkit.getServer().getClass();
            Object globalRegionScheduler = null;
            try {
                for (Method method : serverClass.getMethods()) {
                    if (method.getName().equals("getGlobalRegionScheduler")) {
                        globalRegionScheduler = method.invoke(Bukkit.getServer());
                        break;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            globalRegionScheduler1 = globalRegionScheduler;
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                for (Method method : globalRegionScheduler.getClass().getMethods()) {
                    if (method.getName().equals("execute")) {
                        execute1 = lookup.unreflect(method);
                    }
                    if (method.getName().equals("runAtFixedRate")) {
                        runAtFixedRate1 = lookup.unreflect(method);
                    }
                    if (execute1 != null && runAtFixedRate1 != null) {
                        break;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        globalRegionScheduler = globalRegionScheduler1;
        execute = execute1;
        runAtFixedRate = runAtFixedRate1;
    }

    public void runAsync(Runnable runnable) {
        if (isFolia) {
            try {
                execute.invoke(globalRegionScheduler, plugin, runnable);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public void run(Runnable runnable) {
        if (isFolia) {
            try {
                execute.invoke(globalRegionScheduler, plugin, runnable);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public int runTimer(Runnable runnable, long delay, long period) {
        if (isFolia) {
            try {
                if (delay <= 0) {
                    delay = 1;
                }
                Object res = runAtFixedRate.invoke(globalRegionScheduler, plugin, (Consumer<?>) (task) -> runnable.run(), delay, period);
                Random random = new Random();
                int id = random.nextInt();
                tasks.put(id, res);
                return id;
            } catch (Throwable e) {
                e.printStackTrace();
                return -1;
            }
        } else {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period).getTaskId();
        }
    }

    public void cancel(int id) {
        if (id == -1) {
            return;
        }
        if (isFolia) {
            try {
                tasks.get(id).getClass().getMethod("cancel").invoke(tasks.get(id));
                tasks.remove(id);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().cancelTask(id);
        }
    }
}
