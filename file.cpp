//3.	Дано число K.Подсчитайте, сколько элементов матрицы больше K.K вводится с клавиатуры процессом 0 и рассылается всем процессам.
//Входные данные :
//•	M = 80, N = 70
//•	Случайные числа от 0 до 200
//•	Использовать MPI_Bcast для рассылки K
//•	Использовать MPI_Reduce для подсчета


#include <iostream>
#include <mpi.h>
using namespace std;

void print(int** a,int n,int m) {
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            cout << a[i][j] <<" ";
        }
        cout << "\n";
    }
}

int main(int argc, char* argv[]) {
    MPI_Init(&argc, &argv);
    int size, rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    int k, local_count = 0,ans;

    if (rank == 0) {
        cin >> k;
    }
    MPI_Bcast(&k, 1, MPI_INT, 0, MPI_COMM_WORLD); // рассылка

    int rows = 8, cols = 7;
    int local_rows = rows / size; // то шо хранит каждый поток
    if (rank < rows % size) local_rows++;  // добавляем остаток первым процессам, по штучке каждому
    srand(time(0) + rank);

    int** a = new int* [local_rows];
    for (int i = 0; i < local_rows; i++) {
        a[i] = new int[cols];
        for (int j = 0; j < cols; j++) {
            a[i][j] = rand() % 201;
        }
    }

    // печатаем только свои строки
    for (int p = 0; p < size; p++) {
        if (rank == p) {
            cout << "Process " << rank << "" << endl;
            print(a, local_rows, cols);
            cout.flush();  // принудительно выводим буфер
        }
        MPI_Barrier(MPI_COMM_WORLD);  // ждём, пока текущий процесс допечатает
    }

    for (int i = 0; i < local_rows; i++) {
        for (int j = 0; j < cols; j++) {
            if (a[i][j] > k)local_count += 1;
        }
    }
    MPI_Reduce(&local_count,&ans,1,MPI_INT,MPI_SUM,0,MPI_COMM_WORLD);
    if (rank == 0)cout << "\n\n" << ans;

    MPI_Finalize();
}
