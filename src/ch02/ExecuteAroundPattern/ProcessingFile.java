package modernjavainaction.ch02.ExecuteAroundPattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessingFile {
    public static void main(String[] args) {
        // 순환 패턴: 자원을 열고 -> 작업 process -> 자원을 닫고
        // 실행 어라운드 패턴: 실행을 시킬 작업을 사이에 두고, 자원을 여는 행위와 자원을 닫는 행위로 감싸져있는 형태
        try{
            // try - with resources 로 인해 실행 어라운드 패턴을 유지할 수 있지만
            // 아래는 작업이 고정된 형태다.
//            processFile();
//            System.out.println(processFileByLambda((br) -> br.readLine()));
            // 이제 두줄을 입력받는 작업을 시키는것도 자유로워졌다.
            System.out.println(processFileByLambda((br) -> br.readLine() + br.readLine()));
        }catch(IOException e){
            e.printStackTrace();
        }

    }
    public static String processFile() throws IOException{
        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))){
            br.readLine(); // 실행시킬 작업
            return br.readLine();
        }
    }

    public static String processFileByLambda(BufferedReaderProcessor p) throws IOException{
        // 동작 파리미터화를 달성하기 위해 함수형 인터페이스를 적용시켰다.
        // 이로인해 자원 할당 및 자원 회수 사이에 실행되어질 내용을 외부로부터 전달받는다.
        try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))){
            return p.process(br);
        }

    }
}
