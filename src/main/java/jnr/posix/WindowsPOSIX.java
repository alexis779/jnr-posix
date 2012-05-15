package jnr.posix;

import jnr.constants.platform.Errno;
import static jnr.constants.platform.Errno.*;
import static jnr.constants.platform.windows.LastError.*;
import jnr.ffi.Pointer;
import jnr.ffi.Type;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.provider.jffi.Provider;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import jnr.posix.util.WindowsHelpers;

final class WindowsPOSIX extends BaseNativePOSIX {
    private final static int FILE_TYPE_CHAR = 0x0002;

    private final static Map<Integer, Errno> errorToErrnoMapper
            = new HashMap<Integer, Errno>();

    static {
        errorToErrnoMapper.put(ERROR_INVALID_FUNCTION.value(), EINVAL);
        errorToErrnoMapper.put(ERROR_FILE_NOT_FOUND.value(), ENOENT);
        errorToErrnoMapper.put(ERROR_PATH_NOT_FOUND.value(), ENOENT);
        errorToErrnoMapper.put(ERROR_TOO_MANY_OPEN_FILES.value(), EMFILE);
        errorToErrnoMapper.put(ERROR_ACCESS_DENIED.value(), EACCES);
        errorToErrnoMapper.put(ERROR_INVALID_HANDLE.value(), EBADF );
        errorToErrnoMapper.put(ERROR_ARENA_TRASHED.value(), ENOMEM);
        errorToErrnoMapper.put(ERROR_NOT_ENOUGH_MEMORY.value(), ENOMEM);
        errorToErrnoMapper.put(ERROR_INVALID_BLOCK.value(), ENOMEM);
        errorToErrnoMapper.put(ERROR_BAD_ENVIRONMENT.value(), E2BIG );
        errorToErrnoMapper.put(ERROR_BAD_FORMAT.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_INVALID_ACCESS.value(), EINVAL);
        errorToErrnoMapper.put(ERROR_INVALID_DATA.value(), EINVAL);
        errorToErrnoMapper.put(ERROR_INVALID_DRIVE.value(), ENOENT);
        errorToErrnoMapper.put(ERROR_CURRENT_DIRECTORY.value(), EACCES);
        errorToErrnoMapper.put(ERROR_NOT_SAME_DEVICE.value(), EXDEV );
        errorToErrnoMapper.put(ERROR_NO_MORE_FILES.value(), ENOENT);
        errorToErrnoMapper.put(ERROR_WRITE_PROTECT.value(), EROFS );
        errorToErrnoMapper.put(ERROR_BAD_UNIT.value(), ENODEV);
        errorToErrnoMapper.put(ERROR_NOT_READY.value(), ENXIO );
        errorToErrnoMapper.put(ERROR_BAD_COMMAND.value(), EACCES);
        errorToErrnoMapper.put(ERROR_CRC.value(), EACCES);
        errorToErrnoMapper.put(ERROR_BAD_LENGTH.value(), EACCES);
        errorToErrnoMapper.put(ERROR_SEEK.value(), EIO);
        errorToErrnoMapper.put(ERROR_NOT_DOS_DISK.value(), EACCES);
        errorToErrnoMapper.put(ERROR_SECTOR_NOT_FOUND.value(), EACCES);
        errorToErrnoMapper.put(ERROR_OUT_OF_PAPER.value(), EACCES);
        errorToErrnoMapper.put(ERROR_WRITE_FAULT.value(), EIO);
        errorToErrnoMapper.put(ERROR_READ_FAULT.value(), EIO);
        errorToErrnoMapper.put(ERROR_GEN_FAILURE.value(), EACCES);
        errorToErrnoMapper.put(ERROR_LOCK_VIOLATION.value(), EACCES);
        errorToErrnoMapper.put(ERROR_SHARING_VIOLATION.value(), EACCES);
        errorToErrnoMapper.put(ERROR_WRONG_DISK.value(), EACCES);
        errorToErrnoMapper.put(ERROR_SHARING_BUFFER_EXCEEDED.value(), EACCES);
        errorToErrnoMapper.put(ERROR_BAD_NETPATH.value(), ENOENT);
        errorToErrnoMapper.put(ERROR_NETWORK_ACCESS_DENIED.value(), EACCES);
        errorToErrnoMapper.put(ERROR_BAD_NET_NAME.value(), ENOENT);
        errorToErrnoMapper.put(ERROR_FILE_EXISTS.value(), EEXIST);
        errorToErrnoMapper.put(ERROR_CANNOT_MAKE.value(), EACCES);
        errorToErrnoMapper.put(ERROR_FAIL_I24.value(), EACCES);
        errorToErrnoMapper.put(ERROR_INVALID_PARAMETER.value(), EINVAL);
        errorToErrnoMapper.put(ERROR_NO_PROC_SLOTS.value(), EAGAIN);
        errorToErrnoMapper.put(ERROR_DRIVE_LOCKED.value(), EACCES);
        errorToErrnoMapper.put(ERROR_BROKEN_PIPE.value(), EPIPE);
        errorToErrnoMapper.put(ERROR_DISK_FULL.value(), ENOSPC);
        errorToErrnoMapper.put(ERROR_INVALID_TARGET_HANDLE.value(), EBADF);
        errorToErrnoMapper.put(ERROR_INVALID_HANDLE.value(), EINVAL);
        errorToErrnoMapper.put(ERROR_WAIT_NO_CHILDREN.value(), ECHILD);
        errorToErrnoMapper.put(ERROR_CHILD_NOT_COMPLETE.value(), ECHILD);
        errorToErrnoMapper.put(ERROR_DIRECT_ACCESS_HANDLE.value(), EBADF);
        errorToErrnoMapper.put(ERROR_NEGATIVE_SEEK.value(), EINVAL);
        errorToErrnoMapper.put(ERROR_SEEK_ON_DEVICE.value(), EACCES);
        errorToErrnoMapper.put(ERROR_DIR_NOT_EMPTY.value(), ENOTEMPTY);
        errorToErrnoMapper.put(ERROR_DIRECTORY.value(), ENOTDIR);
        errorToErrnoMapper.put(ERROR_NOT_LOCKED.value(), EACCES);
        errorToErrnoMapper.put(ERROR_BAD_PATHNAME.value(), ENOENT);
        errorToErrnoMapper.put(ERROR_MAX_THRDS_REACHED.value(), EAGAIN);
        errorToErrnoMapper.put(ERROR_LOCK_FAILED.value(), EACCES);
        errorToErrnoMapper.put(ERROR_ALREADY_EXISTS.value(), EEXIST);
        errorToErrnoMapper.put(ERROR_INVALID_STARTING_CODESEG.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_INVALID_STACKSEG.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_INVALID_MODULETYPE.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_INVALID_EXE_SIGNATURE.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_EXE_MARKED_INVALID.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_BAD_EXE_FORMAT.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_ITERATED_DATA_EXCEEDS_64k.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_INVALID_MINALLOCSIZE.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_DYNLINK_FROM_INVALID_RING.value(),ENOEXEC);
        errorToErrnoMapper.put(ERROR_IOPL_NOT_ENABLED.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_INVALID_SEGDPL.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_AUTODATASEG_EXCEEDS_64k.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_RING2SEG_MUST_BE_MOVABLE.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_RELOC_CHAIN_XEEDS_SEGLIM.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_INFLOOP_IN_RELOC_CHAIN.value(), ENOEXEC);
        errorToErrnoMapper.put(ERROR_FILENAME_EXCED_RANGE.value(), ENOENT);
        errorToErrnoMapper.put(ERROR_NESTING_NOT_ALLOWED.value(), EAGAIN);
        // ERROR_PIPE_LOCAL (in MRI)
        errorToErrnoMapper.put(229, EPIPE);
        errorToErrnoMapper.put(ERROR_BAD_PIPE.value(), EPIPE);
        errorToErrnoMapper.put(ERROR_PIPE_BUSY.value(), EAGAIN);
        errorToErrnoMapper.put(ERROR_NO_DATA.value(), EPIPE);
        errorToErrnoMapper.put(ERROR_PIPE_NOT_CONNECTED.value(), EPIPE);
        errorToErrnoMapper.put(ERROR_OPERATION_ABORTED.value(), EINTR);
        errorToErrnoMapper.put(ERROR_NOT_ENOUGH_QUOTA.value(), ENOMEM);
        errorToErrnoMapper.put(ERROR_MOD_NOT_FOUND.value(), ENOENT);
        errorToErrnoMapper.put(WSAENAMETOOLONG.value(), ENAMETOOLONG);
        errorToErrnoMapper.put(WSAENOTEMPTY.value(), ENOTEMPTY);
        errorToErrnoMapper.put(WSAEINTR.value(), EINTR);
        errorToErrnoMapper.put(WSAEBADF.value(), EBADF);
        errorToErrnoMapper.put(WSAEACCES.value(), EACCES);
        errorToErrnoMapper.put(WSAEFAULT.value(), EFAULT);
        errorToErrnoMapper.put(WSAEINVAL.value(), EINVAL);
        errorToErrnoMapper.put(WSAEMFILE.value(), EMFILE);
    }

    WindowsPOSIX(String libraryName, LibCProvider libc, POSIXHandler handler) {
        super(libraryName, libc, handler);
    }
    
    @Override
    public FileStat allocateStat() {
        return new WindowsFileStat(this);
    }

    @Override
    public int kill(int pid, int signal) {
        handler.unimplementedError("kill");

        return -1;
    }

    @Override
    public int chmod(String filename, int mode) {
        byte[] wpath = WindowsHelpers.toWPath(filename);
        return ((WindowsLibC) libc())._wchmod(wpath, mode);
    }
    
    @Override
    public int chown(String filename, int user, int group) {
        return 0;
    }
    
    @Override
    public int exec(String path, String[] argv) {
        if (argv.length == 1) return spawn(true, argv[0], null, path, null);

        return aspawn(true, null, argv, path, null);
    }
    
    @Override
    public int exec(String path, String[] argv, String[] envp) {
        if (argv.length == 1) return spawn(true, argv[0], null, path, envp);

        return aspawn(true, null, argv, path, envp);
    }
    
    @Override
    public int execv(String path, String[] argv) {
        handler.unimplementedError("egid");

        return -1;
    }

    @Override
    public int getegid() {
        handler.unimplementedError("egid");

        return -1;
    }

    @Override
    public int setegid(int egid) {
        handler.unimplementedError("setegid");

        return -1;
    }

    @Override
    public int geteuid() {
        return 0;
    }
    
    @Override
    public String getenv(String envName) {
        handler.unimplementedError("getenv");
        
        return null;
    }

    @Override
    public int seteuid(int euid) {
        handler.unimplementedError("seteuid");

        return -1;
    }

    @Override
    public int getuid() {
        return 0;
    }

    @Override
    public int setuid(int uid) {
        handler.unimplementedError("setuid");

        return -1;
    }

    @Override
    public int getgid() {
        handler.unimplementedError("getgid");

        return -1;
    }

    @Override
    public int setgid(int gid) {
        handler.unimplementedError("setgid");

        return -1;
    }

    @Override
    public int getpgid(int pid) {
        handler.unimplementedError("getpgid");

        return -1;
    }
    
    @Override
    public int getpgid() {
        handler.unimplementedError("getpgid");

        return -1;
    }

    @Override
    public int setpgid(int pid, int pgid) {
        handler.unimplementedError("setpgid");

        return -1;
    }
    
    @Override
    public int getpriority(int which, int who) {
        handler.unimplementedError("getpriority");

        return -1;
    }
    
    @Override
    public int setpriority(int which, int who, int prio) {
        handler.unimplementedError("setpriority");

        return -1;
    }

    @Override
    public int getppid() {
        return 0;
    }
    
    @Override
    public int lchmod(String filename, int mode) {
        handler.unimplementedError("lchmod");
        
        return -1;
    }
    
    @Override
    public int lchown(String filename, int user, int group) {
        handler.unimplementedError("lchown");
        
        return -1;
    }
    
    @Override
    public FileStat lstat(String path) {
        return stat(path);
    }

    @Override
    public String readlink(String oldpath) {
        handler.unimplementedError("readlink");

        return null;
    }
    
    @Override
    public int setenv(String envName, String envValue, int overwrite) {
        if (envName.contains("=")) {
            handler.error(EINVAL, envName);
            return -1;
        }

        // FIXME: We do not have getenv implemented yet.  So we are ignoring for now
        // POSIX specified.  Existence is success if overwrite is 0.
        // if (overwrite == 0 && getenv(envName) != null) return 0;
        
        byte[] wideName = WindowsHelpers.toWString(envName);
        byte[] wideValue = WindowsHelpers.toWString(envValue);
        if (!((WindowsLibC) libc()).SetEnvironmentVariableW(wideName, wideValue)) {
            handler.error(EINVAL, envName);
            return -1;
        }

        return 0;
    }
    
    @Override
    public int unsetenv(String envName) {
        if (!((WindowsLibC) libc()).SetEnvironmentVariableW(WindowsHelpers.toWString(envName), null)) {
            handler.error(EINVAL, envName);
            return -1;
        }
        
        return 0;
    }

    private static final int INVALID_HANDLE_VALUE = -1;

    private static final int GENERIC_ALL = 0x10000000;
    private static final int GENERIC_READ = 0x80000000;
    private static final int GENERIC_WRITE = 0x40000000;
    private static final int GENERIC_EXECUTE = 0x2000000;

    private static final int FILE_SHARE_DELETE = 0x00000004;
    private static final int FILE_SHARE_READ =  0x00000001;
    private static final int FILE_SHARE_WRITE =  0x00000002;

    private static final int CREATE_ALWAYS = 2;
    private static final int CREATE_NEW = 1;
    private static final int OPEN_ALWAYS = 4;
    private static final int OPEN_EXISTING = 3;
    private static final int TRUNCATE_EXISTING = 5;

    public static final int FILE_FLAG_BACKUP_SEMANTICS = 0x02000000;

    @Override
    public int utimes(String path, long[] atimeval, long[] mtimeval) {
        WindowsLibC libc = (WindowsLibC) libc();
        byte[] wpath = WindowsHelpers.toWPath(path);
        FileTime aTime = atimeval == null ? null : unixTimeToFileTime(atimeval[0]);
        FileTime mTime = mtimeval == null ? null : unixTimeToFileTime(mtimeval[0]);

        if (aTime == null || mTime == null) {
            FileTime nowFile = unixTimeToFileTime(System.currentTimeMillis() / 1000L);

            if (aTime == null) aTime = nowFile;
            if (mTime == null) mTime = nowFile;
        }

        int handle = libc.CreateFileW(wpath, GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE,
                null, OPEN_EXISTING, FILE_FLAG_BACKUP_SEMANTICS, 0);
        if (handle == INVALID_HANDLE_VALUE) {
            return -1;             // TODO proper error handling
        }

        boolean timeSet = libc.SetFileTime(handle, null, aTime, mTime);
        libc.CloseHandle(handle);

        return timeSet ? 0 : -1;
    }

    private FileTime unixTimeToFileTime(long unixTime) {
        // FILETIME is a 64-bit unsigned integer representing
        // the number of 100-nanosecond intervals since January 1, 1601
        // UNIX timestamp is number of seconds since January 1, 1970
        // 116444736000000000 = 10000000 * 60 * 60 * 24 * 365 * 369 + 89 leap days
        long ft = (unixTime + 11644473600L) * 10000000L;

        FileTime fileTime = new FileTime(getRuntime());
        fileTime.dwLowDateTime.set(ft & 0xFFFFFFFFL);
        fileTime.dwHighDateTime.set((ft >> 32) & 0xFFFFFFFFL);
        return fileTime;
    }

    @Override
    public int wait(int[] status) {
        handler.unimplementedError("wait");

        return -1;
    }

    @Override
    public int waitpid(int pid, int[] status, int flags) {
        handler.unimplementedError("waitpid");

        return -1;
    }

    @Override
    public String getlogin() {
        return helper.getlogin();
    }

    @Override
    public int endgrent() {
        return 0;
    }

    @Override
    public int endpwent() {
        return helper.endpwent();
    }

    @Override
    public Group getgrent() {
        return null;
    }

    @Override
    public Passwd getpwent() {
        return null;
    }

    @Override
    public Group getgrgid(int which) {
        return null;
    }

    @Override
    public Passwd getpwnam(String which) {
        return null;
    }

    @Override
    public Group getgrnam(String which) {
        return null;
    }
    
    @Override
    public int setgrent() {
        return 0;
    }
    
    @Override
    public int setpwent() {
        return helper.setpwent();
    }

    @Override
    public Passwd getpwuid(int which) {
        return null;
    }

    @Override
    public boolean isatty(FileDescriptor fd) {
        int handle = (int)helper.gethandle(fd);

        int type = ((WindowsLibC)libc()).GetFileType(handle);
        return type == FILE_TYPE_CHAR;
    }

    @Override
    public int mkdir(String path, int mode) {
        byte[] widePath = WindowsHelpers.toWPath(path);
        int res = -1;
        
        if (((WindowsLibC)libc())._wmkdir(widePath) == 0) {
            res = ((WindowsLibC) libc())._wchmod(widePath, mode);
        }
        
        if (res < 0) {
            int errno = errno();
            handler.error(Errno.valueOf(errno), path);
        }
        return res;
    }

    @Override
    public int link(String oldpath, String newpath) {
        byte[] oldWPath = WindowsHelpers.toWPath(oldpath);
        byte[] newWPath = WindowsHelpers.toWPath(newpath);

        boolean linkCreated =  ((WindowsLibC)libc()).CreateHardLinkW(newWPath, oldWPath, null);

        if (!linkCreated) {
            int error = errno();
            handler.error(mapErrorToErrno(error), oldpath + " or " + newpath);
            return error;
        } else {
            return 0;
        }
    }
    
    /**
     * @param overlay is P_OVERLAY if true and P_NOWAIT if false
     * @param program to be invoked
     * @param argv is all args including argv0 being what is executed
     * @param path is path to be searched when needed (delimited by ; on windows)
     * @return the pid
     */    
    public int aspawn(boolean overlay, String program, String[] argv, String path, String[] envp) {
        try {
        if (argv.length == 0) return -1;
        
        String[] cmds = WindowsHelpers.processCommandArgs(this, program, argv, path);
 
        return childResult(createProcess(cmds[0], cmds[1], null, null, null, null, envp), overlay);
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * @param overlay is P_OVERLAY if true and P_NOWAIT if false
     * @param command full command string
     * @param program program to be invoked
     * @param path is path to be searched when needed (delimited by ; on windows)     * 
     * @return the pid
     */
    public int spawn(boolean overlay, String command, String program, String path, String[] envp) {
        if (command == null) return -1;

        String[] cmds = WindowsHelpers.processCommandLine(this, command, program, path);

        return childResult(createProcess(cmds[0], cmds[1], null, null, null, null, envp), overlay);
    }
    
    private int childResult(WindowsChildRecord child, boolean overlay) {
        if (child == null) return -1;

        if (overlay) {
            IntByReference exitCode = new IntByReference();

            WindowsLibC libc = (WindowsLibC) libc();
            int handle = child.getProcess().intValue();
            
            libc.WaitForSingleObject(handle, WindowsLibC.INFINITE);
            libc.GetExitCodeProcess(handle, exitCode);
            libc.CloseHandle(handle);
            System.exit(exitCode.getValue());
        }

        return child.getPid();
    }

    private static Errno mapErrorToErrno(int error) {
        Errno errno = errorToErrnoMapper.get(error);
        if (errno == null) {
            errno = __UNKNOWN_CONSTANT__;
        }
        return errno;
    }

    private static final int STARTF_USESTDHANDLES = 0x00000100;
    
    // Used by spawn and aspawn (Note: See fixme below...envp not hooked up yet)
    private WindowsChildRecord createProcess(String command, String program, 
            WindowsSecurityAttributes securityAttributes, Pointer input,
            Pointer output, Pointer error, String[] envp) {
        if (command == null || program == null) {
            handler.error(EFAULT, "no command or program specified");
            return null;
        }
        
        if (securityAttributes == null) {
            securityAttributes = new WindowsSecurityAttributes(getRuntime());
        }
        
        WindowsStartupInfo startupInfo = new WindowsStartupInfo(getRuntime());
        WindowsLibC libc = (WindowsLibC) libc();
        
        startupInfo.setFlags(STARTF_USESTDHANDLES);
        startupInfo.setStandardInput(input != null ? input :
                libc.GetStdHandle(WindowsLibC.STD_INPUT_HANDLE));
        startupInfo.setStandardOutput(output != null ? output :
                libc.GetStdHandle(WindowsLibC.STD_OUTPUT_HANDLE));
        startupInfo.setStandardError(error != null ? input :
                libc.GetStdHandle(WindowsLibC.STD_ERROR_HANDLE));
        
        int creationFlags = WindowsLibC.NORMAL_PRIORITY_CLASS | WindowsLibC.CREATE_UNICODE_ENVIRONMENT;
        WindowsProcessInformation processInformation = new WindowsProcessInformation(getRuntime());

        // FIXME: Convert envp into useful wideEnv
        Pointer wideEnv = null;
        byte[] programW = WindowsHelpers.toWString(program);
        byte[] cwd = WindowsHelpers.toWString(WindowsHelpers.escapePath(handler.getCurrentWorkingDirectory().toString()) +"\\");
        ByteBuffer commandW = ByteBuffer.wrap(WindowsHelpers.toWString(command));
        boolean returnValue = libc.CreateProcessW(programW, commandW, 
                securityAttributes, securityAttributes, 
                securityAttributes.getInheritHandle() ? 1: 0, creationFlags, wideEnv, cwd, 
                startupInfo, processInformation);
        
        if (!returnValue) return null;
        
        libc.CloseHandle(processInformation.getThread());
        
        // TODO: On winnt reverse sign of pid
        return new WindowsChildRecord(processInformation.getProcess(), processInformation.getPid());
    }

    public static final PointerConverter PASSWD = new PointerConverter() {
        public Object fromNative(Object arg, FromNativeContext ctx) {
            throw new RuntimeException("no support for native passwd");
        }
    };
}
