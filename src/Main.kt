// ==========================================
// Prática 1: Contador com ViewModel (Exercícios)
// ==========================================
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

data class ContadorUiState(
    val contador: Int = 0,
    val historico: List<String> = emptyList(),
    val limiteMaximo: Int = 100,
    val avisoLimite: String? = null
)

class ContadorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ContadorUiState())
    val uiState: StateFlow<ContadorUiState> = _uiState.asStateFlow()

    fun incrementar() {
        val estadoAtual = _uiState.value
        if (estadoAtual.contador < estadoAtual.limiteMaximo) {
            _uiState.update { 
                it.copy(
                    contador = it.contador + 1,
                    historico = it.historico + "+1",
                    avisoLimite = null
                )
            }
        } else {
            _uiState.update { it.copy(avisoLimite = "Limite máximo atingido!") }
        }
    }

    fun decrementar() {
        if (_uiState.value.contador > 0) {
            _uiState.update { 
                it.copy(
                    contador = it.contador - 1,
                    historico = it.historico + "-1",
                    avisoLimite = null
                )
            }
        }
    }

    fun resetar() {
        _uiState.update { 
            it.copy(
                contador = 0,
                historico = it.historico + "reset",
                avisoLimite = null
            )
        }
    }

    fun setValorInicial(valor: Int) {
        _uiState.update {
            it.copy(
                contador = valor,
                historico = it.historico + "início: $valor",
                avisoLimite = null
            )
        }
    }
}

@Composable
fun ContadorScreen(viewModel: ContadorViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var valorInicialTexto by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Contador", style = MaterialTheme.typography.headlineMedium)
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = valorInicialTexto,
                onValueChange = { valorInicialTexto = it },
                label = { Text("Valor inicial") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                val valor = valorInicialTexto.toIntOrNull()
                if (valor != null) viewModel.setValorInicial(valor)
                valorInicialTexto = ""
            }) {
                Text("Definir")
            }
        }

        Text(text = "${uiState.contador}", fontSize = 64.sp)
        
        uiState.avisoLimite?.let { 
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { viewModel.decrementar() }) { Text("−") }
            Button(onClick = { viewModel.incrementar() }) { Text("+") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = { viewModel.resetar() }) {
            Text("Resetar")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Histórico de Operações:", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(uiState.historico) { operacao ->
                Text(text = operacao)
            }
        }
    }
}


// ==========================================
// Prática 2: Lista de Tarefas (Todo List) com MVVM
// ==========================================
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

data class Tarefa(val id: Int, val titulo: String, val concluida: Boolean = false)

data class TarefaUiState(
    val tarefas: List<Tarefa> = emptyList(),
    val textoNovoItem: String = "",
    val mensagem: String? = null,
    val filtroAtual: String = "Todas",
    val textoPesquisa: String = ""
)

class TarefaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TarefaUiState())
    val uiState: StateFlow<TarefaUiState> = _uiState.asStateFlow()
    private var proximoId = 1

    fun atualizarTexto(texto: String) {
        _uiState.update { it.copy(textoNovoItem = texto) }
    }
    
    fun atualizarPesquisa(texto: String) {
        _uiState.update { it.copy(textoPesquisa = texto) }
    }
    
    fun selecionarFiltro(filtro: String) {
        _uiState.update { it.copy(filtroAtual = filtro) }
    }

    fun adicionarTarefa() {
        val texto = _uiState.value.textoNovoItem.trim()
        if (texto.isBlank()) {
            _uiState.update { it.copy(mensagem = "Digite o título da tarefa.") }
            return
        }
        _uiState.update { estado ->
            estado.copy(
                tarefas = estado.tarefas + Tarefa(id = proximoId++, titulo = texto),
                textoNovoItem = "",
                mensagem = null
            )
        }
    }

    fun alternarConclusao(id: Int) {
        _uiState.update { estado ->
            estado.copy(
                tarefas = estado.tarefas.map { tarefa ->
                    if (tarefa.id == id) tarefa.copy(concluida = !tarefa.concluida) else tarefa
                }
            )
        }
    }

    fun removerTarefa(id: Int) {
        _uiState.update { estado ->
            estado.copy(tarefas = estado.tarefas.filter { it.id != id })
        }
    }
    
    fun removerTodasConcluidas() {
        _uiState.update { estado ->
            estado.copy(tarefas = estado.tarefas.filterNot { it.concluida })
        }
    }

    fun limparMensagem() {
        _uiState.update { it.copy(mensagem = null) }
    }
}

@Composable
fun TarefaScreen(viewModel: TarefaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Minhas Tarefas", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = uiState.textoPesquisa,
                onValueChange = viewModel::atualizarPesquisa,
                label = { Text("Pesquisar tarefas...") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.textoNovoItem,
                    onValueChange = viewModel::atualizarTexto,
                    label = { Text("Nova tarefa") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = viewModel::adicionarTarefa) { Text("Adicionar") }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Todas", "Pendentes", "Concluídas").forEach { filtro ->
                    FilterChip(
                        selected = uiState.filtroAtual == filtro,
                        onClick = { viewModel.selecionarFiltro(filtro) },
                        label = { Text(filtro) }
                    )
                }
            }
            
            Button(onClick = viewModel::removerTodasConcluidas, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("Remover todas as concluídas")
            }

            val tarefasFiltradas = uiState.tarefas
                .filter { if (uiState.filtroAtual == "Pendentes") !it.concluida else if (uiState.filtroAtual == "Concluídas") it.concluida else true }
                .filter { it.titulo.contains(uiState.textoPesquisa, ignoreCase = true) }

            if (tarefasFiltradas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhuma tarefa para exibir. 😊") }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(tarefasFiltradas, key = { it.id }) { tarefa ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = tarefa.concluida, onCheckedChange = { viewModel.alternarConclusao(tarefa.id) })
                            Text(text = tarefa.titulo, modifier = Modifier.weight(1f).padding(start = 4.dp))
                            IconButton(onClick = { viewModel.removerTarefa(tarefa.id) }) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}


// ==========================================
// Prática 3: Carregamento Assíncrono com UiState
// ==========================================
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign

sealed interface QuoteUiState {
    data object Loading : QuoteUiState
    data object Empty : QuoteUiState
    data class Success(val frase: String, val autor: String) : QuoteUiState
    data class Error(val mensagem: String) : QuoteUiState
}

class QuoteRepository {
    private val frases = listOf(
        "A simplicidade é a sofisticação máxima." to "Leonardo da Vinci",
        "O sucesso é a soma de pequenos esforços repetidos dia após dia." to "Robert Collier",
        "Aprenda como se fosse viver para sempre." to "Mahatma Gandhi"
    )

    suspend fun buscarFraseAleatoria(): Pair<String, String>? {
        delay(1500)
        val sorteio = Random.nextFloat()
        return when {
            sorteio < 0.2f -> throw Exception("Falha ao conectar ao servidor.")
            sorteio < 0.4f -> null // Simula lista vazia (Empty)
            else -> frases.random()
        }
    }
}

class QuoteViewModel(private val repository: QuoteRepository = QuoteRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<QuoteUiState>(QuoteUiState.Loading)
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()
    
    private val _favoritos = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val favoritos: StateFlow<List<Pair<String, String>>> = _favoritos.asStateFlow()
    
    private val _buscasTotais = MutableStateFlow(0)
    val buscasTotais: StateFlow<Int> = _buscasTotais.asStateFlow()

    init { buscarFrase() }

    fun buscarFrase() {
        viewModelScope.launch {
            _uiState.value = QuoteUiState.Loading
            _buscasTotais.update { it + 1 } // Adiciona ao contador de buscas
            try {
                val resultado = repository.buscarFraseAleatoria()
                if (resultado != null) {
                    _uiState.value = QuoteUiState.Success(resultado.first, resultado.second)
                } else {
                    _uiState.value = QuoteUiState.Empty // Retorna Empty state
                }
            } catch (e: Exception) {
                _uiState.value = QuoteUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    fun favoritar(frase: String, autor: String) {
        val novaFrase = frase to autor
        if (!_favoritos.value.contains(novaFrase)) {
            _favoritos.update { it + novaFrase }
        }
    }
}

@Composable
fun QuoteScreen(viewModel: QuoteViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buscasTotais by viewModel.buscasTotais.collectAsStateWithLifecycle()
    val favoritos by viewModel.favoritos.collectAsStateWithLifecycle()
    
    var verFavoritos by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Total de buscas: $buscasTotais", style = MaterialTheme.typography.labelLarge)
            Button(onClick = { verFavoritos = !verFavoritos }) { Text(if (verFavoritos) "Ver Frases" else "Ver Favoritos") }
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (verFavoritos) {
            FavoritosScreen(favoritos = favoritos)
        } else {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                when (val state = uiState) {
                    is QuoteUiState.Loading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Buscando frase...")
                        }
                    }
                    is QuoteUiState.Empty -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Nenhuma frase encontrada no momento (vazio).", textAlign = TextAlign.Center)
                            Button(onClick = { viewModel.buscarFrase() }) { Text("Tentar novamente") }
                        }
                    }
                    is QuoteUiState.Success -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(text = "\"${state.frase}\"", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                            Text(text = "— ${state.autor}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.favoritar(state.frase, state.autor) }) { Text("Favoritar") }
                                Button(onClick = { viewModel.buscarFrase() }) { Text("Nova frase") }
                            }
                        }
                    }
                    is QuoteUiState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("❌ ${state.mensagem}", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.buscarFrase() }) { Text("Tentar novamente") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritosScreen(favoritos: List<Pair<String, String>>) {
    if (favoritos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Você ainda não tem favoritos.")
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(favoritos) { fraseAutor ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("\"${fraseAutor.first}\"", style = MaterialTheme.typography.bodyLarge)
                        Text("— ${fraseAutor.second}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}