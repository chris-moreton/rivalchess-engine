package com.netadapt.rivalchess.engine;

import com.netadapt.rivalchess.model.BoardModel;
import com.netadapt.rivalchess.util.BitwiseOperation;
import com.netadapt.rivalchess.util.ChessBoardConversion;
import com.netadapt.rivalchess.util.Numbers;

import java.util.Random;

public final class EngineChessBoard 
{
	public long m_pieceBitboards[];
	private long m_pieceBitboardsBackup[][];
	
	public int m_pieceLocations[][];
	private int m_pieceLocationsBackup[][][];
	
	public int m_pieceCounters[];
	private int m_pieceCountersBackup[][];
	
	public boolean m_isWhiteToMove;
	private boolean m_isWhiteToMoveBackup[];

	public byte m_whiteKingSquare;
	private byte m_whiteKingSquareBackup[];

	public byte m_blackKingSquare;
	private byte m_blackKingSquareBackup[];
	
	public boolean m_whiteHasCastled;
	public boolean m_whiteHasCastledBackup[];

	public boolean m_blackHasCastled;
	public boolean m_blackHasCastledBackup[];

	public int m_moveList[];
	
	private int m_numBackups;

	public Bitboards m_bitboards;
	
	public boolean m_isOnNullMove = false;
	
	protected int[] m_legalMoves;
	protected int m_numLegalMoves;
	
	private final static long[][] m_pieceHashValues =
	{
	{2757563266877852572L,6372315729141069125L,6531282879677255786L,6217800311556484852L,4028653356764102261L,7173945121263241432L,3140540760890330300L,186456652255508025L,4326822066600281601L,1676275713585176313L,994786814496869094L,7266780370156599753L,1993832195284356489L,8906581877710625494L,13143720840670621L,4898713441463880900L,2606168633944391863L,1278377858051695283L,1882005865304517078L,3567997896755807464L,2122005311604240155L,2319007812912636961L,4341526477626483922L,4112961148281161054L,1873977493594566140L,305729391848946031L,1737015787209404385L,2237900801421661035L,3194938434899723208L,6533797044794123108L,53098511802011562L,3081966774796706547L,1696652368246260792L,2862724276072767459L,1287517088619610318L,2602374675812111264L,4756084200418510689L,5276272864338057212L,8687878729112386221L,4135797612500512402L,5514543711318410338L,5560689077408854053L,2256144847160470449L,4417513483083909270L,3035972007832032975L,7368920397373182932L,6920167683995549487L,6247308382891571962L,5676121683487873905L,9175320351448874372L,5910720470467824693L,907420310966211028L,6716488881646089174L,5928558823587778993L,5915781820118628492L,39463936643324408L,5811376027078466953L,6523428793023374251L,5956533780815835809L,3641790615902344529L,7353877805870994894L,939029069031068496L,8125675455391727839L,4589414129899697073L,},
	{706413976754998224L,5237446959228702633L,4502343349913478046L,4284922253622685935L,794556809418481704L,6752480058157658297L,5122029621984886652L,6882057774524096198L,6541274272475607484L,1574242302337288143L,111900223082258093L,6631459239802814568L,526646747656935902L,3357192273790967373L,3598688189568699342L,5546366303086541876L,1726594687531813407L,4987647731489328978L,3580842832605597181L,7743075973487833223L,2776180945377874707L,6742207452261577653L,3524831375196424107L,7614586915232374818L,6115393728617541508L,1125662212014421892L,231458207239155535L,2885178576595681658L,2690121602411998221L,3533102710711774971L,7740701900047567421L,1255503606430395821L,8399862489813273836L,1439234334975020321L,4763417158037960862L,4977818468183857538L,4984644494470203422L,4351639087268581437L,2899815003034077612L,9108461806324676569L,5172153358648562244L,2597331834528227009L,6109954286982553472L,2912933226757411468L,7640543903334245893L,2844971232928040881L,2279605498837990134L,3463731465304653534L,76901528508813291L,6280005240992472692L,3260455415836512746L,4131337061747131489L,1402852472918327774L,5102723766602758144L,2949259100454305372L,1944378517589392750L,6606049276124010842L,4945875818949224017L,6463697859664636677L,4563130144202493590L,1130798484264037885L,4668778934824795676L,4808737661328924273L,3266976802810975809L,},
	{3526724629200970276L,48526046699759754L,8167123585693394048L,7126737563418726157L,3884530645993766909L,7040664248638881451L,5837271942644127481L,3616987892652505963L,4083408828443113930L,5348706984051486926L,7306942631434919894L,7788911306385470611L,1920797669047726069L,5767621404457930932L,4939777947758921246L,6828298627902150213L,4582988639121771633L,2721189556933358956L,496370117853838243L,8143452293799687270L,6002576524389228294L,8411950427977459053L,1823534971628121651L,3983150712003752658L,2783514311632729460L,9094688704651908402L,1682895755282384617L,3213042356150788443L,6174516486991242124L,5429085076595783743L,3381254759730602595L,5479816068083974802L,8204697593884991320L,6347530531368040414L,4490397025128192141L,8407408328672604196L,5997021339967186156L,1947076232270972848L,6201620772051583256L,7744342598707609147L,1939499042085219765L,6834867949355847005L,5596041422517147349L,6345497246432346170L,1231538645575753339L,8235430625516224931L,2583608123937963550L,6795223553357527787L,6281788933568863916L,2216591880958825895L,1723055813825809089L,8462832260593510096L,3096213975196258774L,7182992208714122364L,5137375666796102465L,4641310499159706793L,8255850789418445260L,3895969293116932325L,202121942194941618L,3001032906906561674L,4817543641936854517L,2933098853597619575L,1729709981808028074L,2905624112688754045L,},
	{4269252335467993961L,8855045279702453407L,7395403973528249356L,4022410470817470542L,7372074797706027983L,5872608254904647593L,5065301752116643350L,7688499207936066304L,3218757409228525372L,4651989491941495962L,6503996956873938409L,489241169476201417L,8609457659388063253L,2646243653538414237L,4447266732656982996L,193915167707836644L,4529961767168652L,4737883173481870005L,6302424584209810931L,338657460727683711L,1563300542944841483L,5327156014778687932L,2870495655378563278L,5608507905247097283L,3590216443108144923L,2875661783927563348L,7124359522283911602L,8428430503568743857L,4767059299910687386L,2590273633516971606L,4761562871388918166L,2652204195911211977L,8826896887974165415L,1065176361428595232L,9157163046871280177L,4502759812759664448L,5726299958902904109L,530072053618769216L,895846526316524618L,467937357922516750L,8602063605459535850L,6719205568977338381L,8583693595904718437L,4153004816965705917L,5966796638923899027L,1032034806207365317L,1307506363588584492L,3712735712719939276L,4343944053518729075L,1282423760452505319L,173424631240495461L,8381780405369056950L,2199954421593380451L,444917898291442463L,6480926845079875269L,7537216052377744129L,3774733988261462373L,7499074026023255496L,7607843325131718492L,6093993311717792743L,2331685763872572587L,4352876686499200149L,4235202761664779967L,5895241459534232072L,},
	{5482059440352445727L,7956339969058211934L,4954060434318501117L,8115995729820763036L,5721347636312841333L,2551590848249369923L,8761151027090434770L,1450048539819847742L,809275049474077988L,3913411587935094951L,7099828467142482864L,1026992368184896896L,3925751325938633014L,9074419762504123157L,1246437869013631664L,7378047233887314923L,474504446116046695L,681026732928034876L,7503100584678545241L,5144675340194602534L,3281803653846062286L,4706541021557088044L,6352787564453770705L,3307736953582204212L,925057422725221765L,4328643808080894957L,1442583851450410906L,437666655235918844L,7750516601172680244L,7066318828270012948L,8430491571929385137L,9058449950227869823L,5757510486901178080L,7279843450518321304L,43127866548369872L,7801665669407941225L,869472628767054139L,4455912880072542546L,4776599164076577267L,4995651884202018016L,9205339851548284274L,2991217360619359484L,8113533920899574669L,2725531378807444516L,1903209836595839133L,869792416915504714L,2624383920180508576L,7463622451271065457L,4031369777553205636L,8198744696199806783L,6209155299517931176L,7716991479317077036L,8552324997505406292L,5190792594819684254L,5405583483881399091L,1412560298191341700L,103316124955288142L,2570802615516381813L,2742678712837978301L,4736800798767083968L,4689925640499760316L,2011713206560716592L,7698839976705558703L,2101693932821614576L,},
	{9134090328332492302L,5263125285456236617L,2002707346266872506L,8457109776747425942L,6277262185843779334L,1261513154258218264L,743595506557340024L,5118696197521219182L,6389320403900203415L,4792717765137656718L,4590599381035109524L,4633000749212460643L,8853302209228134548L,829933116792773481L,7454206074684803970L,1331564509239791452L,2434809458655800162L,7528690117223614777L,7112175028828574739L,889991979395197613L,3510095911311826305L,6167459476425399399L,8472822513950460484L,878660785199353768L,4718343879738261675L,5624193844673635964L,5799899023464378991L,1093009618003491558L,4513119637856023694L,2754385592750650280L,3524340711222428242L,1204651937704060576L,2768034571782041027L,5845313009030790673L,2379638552628418965L,705221492190164429L,5692332855001729224L,5134048240937163093L,7862354069605444682L,4652522373255046423L,6562611794933572479L,7225846535560570563L,6798374679349098073L,1923114751136460212L,440770511502372502L,4134483883707043431L,2506428789614300520L,3546362436556191123L,3497886265811279489L,932522976137894852L,5443410947940562661L,7395179419574312861L,1925597547461550297L,3083566288934567853L,6391724655495198176L,8164844593195065182L,2909857915875545973L,1053375534840206791L,1296244821721594611L,263126646077365181L,3777884168630782348L,1152166463363885612L,9217013729487695673L,1775948033234846362L,},
	{5691615292360083080L,762190898621373795L,5821639506728817460L,1738661259230741628L,3019431854270606861L,83552056897441975L,6609846280203152870L,435371541937689352L,2282570952197666100L,595876304234693565L,9147403099394789001L,7533035522525488026L,5691934491529333253L,2980086244887624376L,8013905283156048390L,363527508688688673L,7374259252667187068L,362681824165828228L,4847910485499809639L,2253055107480512507L,111529475954971157L,4278544012352367756L,2766192083051374023L,7516265602420957726L,5014460317765925511L,659359411695663573L,5386737622117173576L,1038915006615490181L,7919396185844987858L,1172731710242206002L,7494156588627708487L,6160424331092024832L,4326581697273687236L,7014216158415520524L,5574249326673472790L,1730768916092988523L,1445837622325011940L,3821127040008388125L,1111961441971770546L,1611143702751896527L,6396075583328649777L,7218836424211858955L,1793189800360944425L,7339153043257517224L,6738837834036705714L,3372066062997567342L,7042377330485208733L,5844450985688756751L,3227307292985264729L,5166434995508659230L,4407216685436238279L,785947468814544316L,5494797069623248625L,9064334486126848534L,862322936918398010L,5073869142572136099L,14625017500494987L,6176046267152986925L,2976819429600612209L,4310591706489711250L,6681320062885496862L,8989889258273935260L,3722163576311367700L,1805927480862668597L,},
	{1403565454462916255L,9218273974760043426L,6983677847517772240L,7331591968540275878L,4094079603891112668L,3645458482569787123L,5885980300442319723L,3876507597005764248L,6234295781416942034L,3610081461213122612L,1773637784319159881L,7172009397515594175L,8272488767347050677L,3757391311643631956L,8704162202920884023L,8839713473012409630L,3305289356052602520L,4395246859341533893L,4853354755476472394L,6088014623487808889L,8527577002770203673L,4619286165257909464L,5197689722597111352L,1424875007737058133L,1071196320061364944L,8209690818139668736L,3965369329144655185L,5944407338050120738L,1061778754555020954L,7886209233372012600L,5092299202595764466L,7140620755769072024L,8826446383409644231L,2139401308830923225L,6628160763980947324L,1289382631800531686L,4910706469776067051L,6714723210355708126L,6576358097832606283L,2087337905894618112L,2228344893847779585L,5204208396506408030L,8019687633900796640L,2927389921365747343L,6979770986371552658L,710955057447159835L,1246835428103661368L,4288239899209224309L,414649854433031187L,3098469592891386876L,5644636714708657522L,8397734769590269397L,7561745469996983081L,6916035008389787065L,6700392039785659203L,8055163598027508892L,3826991975611290873L,4673557597793689875L,7034777241825369919L,4770906406929878243L,1061103761288165190L,7135194294936601439L,7406295689631352094L,1113272196100689149L,},
	{1966450248190379088L,2993865635034902059L,8788203921705410991L,9168583593820377450L,1237464767808744413L,1776894105739964056L,5473776405481206593L,3108517860177137431L,5682329741821421590L,9183530200379101140L,3797186426917148354L,1931394037295312024L,2148957555106458413L,8223722867305992927L,1527282297679490533L,5686607043513838725L,3119975448559538420L,8971725973451123922L,4670712296639275156L,7952526744630924527L,8112254140019974140L,1098180819886001975L,2195958098201965283L,146480965931442822L,1000133041828712324L,8859715321010272914L,7713927785108031934L,4664478895417600120L,323237241289302876L,2029739124449746224L,5314045977537512672L,7761665812734607116L,7389606616456274226L,5853730557960210518L,6405457160064940081L,7643336737387839820L,801209189917229092L,4227962273868069596L,3067822289957023955L,133447937448989907L,7326153904809858610L,3347788873011704914L,2542198793675049436L,3933137803842118338L,4740396057898551661L,8202669349863677922L,7840482582246345144L,1176356952632570992L,5769590063640359052L,8782901109905408165L,329571181494047202L,3879829922156060555L,962783806119609348L,6701909345750326547L,1879670639718369096L,6664022440892888548L,5701413165223646902L,418569841824620243L,3586682514319447750L,1154800286085849790L,3158280596792866687L,3691038129842476001L,3264887083481036557L,8923601683697323768L,},
	{4978977443718634328L,7288401877211219539L,507448495946020687L,261342561650965313L,9103401774020839501L,2811272625307151485L,6136143827755009479L,2513761986502584347L,2618037343322452944L,5139270459733357277L,6838266761075862697L,5792000861464993307L,797495321118367286L,1689440298187033107L,7742957107729489807L,108982139420953125L,1368770425759240784L,8975818365220101167L,5171348940803071326L,7429096152147114621L,8439122944226245896L,8969861888678684724L,4496306846960821643L,1249649415168978015L,7224215098383632731L,7234204955776561756L,1908939853186866432L,4613046688406647723L,979409278753174233L,5314833019430831516L,6320929885571283525L,4506518407239481613L,9191767324412116007L,534728121186412623L,4589263512943122260L,7595055229592911986L,4136038448724948633L,666042454987324494L,559356573438353282L,2497096404016838989L,876772448588838374L,2334172993029750454L,478558549750751171L,5692644283346631657L,1608177764376920769L,8203657446035958755L,3560658139861536498L,7969854509332728097L,766225182024402454L,7230810442083413000L,3409645618456651471L,8747460773660686810L,1156376364374428434L,259907978959573025L,1134256150512529358L,9191467588711542031L,3144543534942117624L,4615473031590185602L,1021874242214398643L,507301541282945445L,3340389840876346908L,814010765659554563L,8230058377636026554L,8338979323633225138L,},
	{1918666253752120829L,5123785239787297170L,8260467834230340948L,720956868684585182L,2625801173904837038L,3442049256491471718L,2924963729754111467L,2932143463216750042L,4141774046760525340L,7124333084796562409L,4583803384565297833L,540105885968327263L,6534998781351102301L,7603388969104123373L,1357367707300989722L,451936749081903360L,360461024317963340L,5525004701282371602L,8202188898962120645L,2226657532892967402L,4613010720567674697L,7359905980603219269L,6826810207863871489L,3894350494685017003L,6695962586797368461L,8016002439923018986L,7917644687140354836L,7843954652503033120L,6483045143331120470L,3771306383775362797L,8623750330209127396L,2900091261158332736L,5663671944283168781L,7446022214448060918L,3567290127817993180L,6013836022251678654L,489903320583615529L,2213753398843631372L,785546206712758176L,8097177210502977517L,6939208963085844221L,6469328468874844424L,672985058695375740L,4161058687295838148L,7941065023635809346L,6151778816897791754L,9013150994096788929L,2576155270622500110L,5189079453494188777L,7178303791130062751L,4730696639737107366L,6497451371231361292L,3305587776220532337L,5299835837372005486L,4399911704324168620L,4565360990101716732L,7837932032562291850L,7356609075281478700L,2671276588638382746L,4284991231858828143L,8609346109930705370L,3737809786861683657L,7460576325325589108L,76027739059752509L,},
	{9208188818834422943L,5079652584320734989L,5625753500461165109L,2522764131739476098L,922397543745308399L,1849530567864289883L,3565871539435181739L,8501945789448278742L,2834020835440806116L,2114898699739419510L,8156849327267637025L,7417872299570049773L,760442579520793625L,8125352974898441086L,7372747514993796401L,4820007705677169948L,5094849792765808884L,8654594184840000990L,1043002158891838519L,30069737146852339L,7480840281579044543L,5617509422479735054L,8087298089913277531L,2732971805427057233L,2139833233488709521L,4021782638741383288L,8661487598710505540L,4811833499348751086L,6129950681643071213L,4462470102654327859L,5370869476704701078L,2247623128255100219L,6095943374180634947L,4932188522700420147L,4913325323529309583L,3493168178410901252L,3408574234029055147L,1781269879077519466L,6009011768553083766L,7359460891617983503L,4078903063976677065L,1891844119477713966L,3111342753299638506L,8498343264139653994L,8554521356288376209L,8647006971813776110L,1405867315492784046L,3261249132213245190L,4172321625119480057L,1422367203506234086L,6260367524173538434L,608749605062299397L,2045889148659716268L,2122806103854230142L,8839124020569954204L,8418931016733783342L,5466714312773596092L,2059608657860469959L,1622528598239002687L,228144609206946361L,5588235677104977126L,3157823198315106642L,1729483203816602699L,3116580019810902115L}
	};

	private final static long[] m_moverHashValues = {6612194290785701391L,7796428774704130372L};
	private final static long m_hashSwitchMovers = m_moverHashValues[RivalConstants.WHITE] ^ m_moverHashValues[RivalConstants.BLACK];
	
	public long m_hashValue = 1427869295504964227L;
	public long m_whitePawnHashValue = 5454534288458826522L;
	public long m_blackPawnHashValue = 2337497004321174159L;
	
	public long m_whitePawnHashValueBackup[];
	public long m_blackPawnHashValueBackup[];
	private long[] m_hashValueBackup;
	
	private int pawnSquares[];
	private long bitboardMaskForward[];
	private long bitboardMaskCapture[];
	private long bitboardPawnMoves;
	private long bitboardJumpMoves;
	
	public EngineChessBoard(Bitboards bitboards)
	{
		this.m_bitboards = bitboards;
		initArrays();
	}
	
	public void setBoard(BoardModel board)
	{
		this.m_numBackups = 0;
		setBitboards(board);
		initHash();
	}
	
	private void initArrays()
	{
		if (RivalConstants.TRACK_PIECE_LOCATIONS)
		{
			this.m_pieceLocationsBackup = new int[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES][12][9];
			this.m_pieceCountersBackup = new int[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES][12];
		}
		this.m_pieceBitboardsBackup = new long[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES][RivalConstants.NUM_BITBOARDS];
		this.m_whiteKingSquareBackup = new byte[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES];
		this.m_blackKingSquareBackup = new byte[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES];
		this.m_whiteHasCastledBackup = new boolean[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES];
		this.m_blackHasCastledBackup = new boolean[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES];
		this.m_whitePawnHashValueBackup = new long[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES];
		this.m_blackPawnHashValueBackup = new long[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES];
		this.m_isWhiteToMoveBackup = new boolean[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES];
		this.m_moveList = new int[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES];
		this.m_hashValueBackup = new long[RivalConstants.MAX_TREE_DEPTH + RivalConstants.MAX_GAME_MOVES];
	}
	
	private void initHash()
	{
		/*this.m_moverHashValues = new long[2];

		Random r = new Random(RivalConstants.HASH_SEED);
		
		for (int piece=RivalConstants.WP; piece<=RivalConstants.BR; piece++)
		{
			System.out.print("{");
			for (int square=0; square<64; square++)
			{
				this.m_pieceHashValues[piece][square] = Numbers.getRandomUnsignedLong(r);
				System.out.print(this.m_pieceHashValues[piece][square] + "L,");
			}
			System.out.println("},");
		}
		
		this.m_moverHashValues[RivalConstants.WHITE] = Numbers.getRandomUnsignedLong(r);
		this.m_moverHashValues[RivalConstants.BLACK] = Numbers.getRandomUnsignedLong(r);

		System.out.println(this.m_moverHashValues[RivalConstants.WHITE]);
		System.out.println(this.m_moverHashValues[RivalConstants.BLACK]);
		
		this.m_hashSwitchMovers = this.m_moverHashValues[RivalConstants.WHITE] ^ this.m_moverHashValues[RivalConstants.BLACK];
		
		this.m_hashValue = Numbers.getRandomUnsignedLong(r);
		this.m_whitePawnHashValue = Numbers.getRandomUnsignedLong(r);
		this.m_blackPawnHashValue = Numbers.getRandomUnsignedLong(r);

		System.out.println(this.m_hashValue);
		System.out.println(this.m_whitePawnHashValue);
		System.out.println(this.m_blackPawnHashValue); */

		//System.exit(0);

		for (int bitNum=0; bitNum<64; bitNum++)
		{
			for (int piece=RivalConstants.WP; piece<=RivalConstants.BR; piece++)
			{
				if ((this.m_pieceBitboards[piece] & (1L << bitNum)) != 0)
				{
					this.m_hashValue ^= this.m_pieceHashValues[piece][bitNum];
					if (piece == RivalConstants.WP)
					{
						this.m_whitePawnHashValue ^= this.m_pieceHashValues[piece][bitNum];
					}
					if (piece == RivalConstants.BP)
					{
						this.m_blackPawnHashValue ^= this.m_pieceHashValues[piece][bitNum];
					}
				}
			}
		}
		
		this.m_hashValue ^= this.m_moverHashValues[m_isWhiteToMove ? RivalConstants.WHITE : RivalConstants.BLACK];
	}

	public void resetLegalMoves()
	{
		this.m_numLegalMoves = 0;
		this.m_legalMoves[this.m_numLegalMoves] = 0;
	}

	public void generateStraightSlidingMovesWithLoops(long bitmapPiecesToMove)
	{
		int pieceSquares[];
		int bitRef;
		int i, j;

		pieceSquares = Bitboards.getSetBits(bitmapPiecesToMove);
		for (j=0; pieceSquares[j] != -1; j++)
		{
			bitRef = pieceSquares[j];
			
			for (i=bitRef+8; i<=63; i+=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.ENEMY] & (1L << i)) != 0) {
						this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
					}
					break;
				}
				this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
			}			

			for (i=bitRef-8; i>=0; i-=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.ENEMY] & (1L << i)) != 0) {
						this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
					}
					break;
				}
				this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
			}			

			for (i=bitRef+1; i%8!=0; i++) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.ENEMY] & (1L << i)) != 0) {
						this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
					}
					break;
				}
				this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
			}			

			for (i=bitRef-1; i%8!=7 && i>=0; i--) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.ENEMY] & (1L << i)) != 0) {
						this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
					}
					break;
				}
				this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
			}			
		}	
	}
	
	public void generateDiagonalSlidingMovesWithLoops(long bitmapPiecesToMove)
	{
		int pieceSquares[];
		int bitRef;
		int i, j;
		
		pieceSquares = Bitboards.getSetBits(bitmapPiecesToMove);
		for (j=0; pieceSquares[j] != -1; j++)
		{
			bitRef = pieceSquares[j];
			
			for (i=bitRef+9; i%8!=0 && i<=63; i+=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.ENEMY] & (1L << i)) != 0) {
						this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
					}
					break;
				}
				this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
			}			

			for (i=bitRef-9; i%8!=7 && i>=0; i-=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.ENEMY] & (1L << i)) != 0) {
						this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
					}
					break;
				}
				this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
			}			

			for (i=bitRef+7; i%8!=7 && i<=63; i+=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.ENEMY] & (1L << i)) != 0) {
						this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
					}
					break;
				}
				this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
			}			

			for (i=bitRef-7; i%8!=0 && i>=0; i-=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.ENEMY] & (1L << i)) != 0) {
						this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
					}
					break;
				}
				this.m_legalMoves[this.m_numLegalMoves++] = (bitRef << 16) | i;
			}			
		}	
	}
	
	public void generateDiagonalSlidingMovesWithBitboards(long bitmapPiecesToMove)
	{
		long bitboardMoves;
		int pieceSquares[];
		int occupancy;
		int bitRef;
		
		pieceSquares = Bitboards.getSetBits(bitmapPiecesToMove);		
		for (int i=0; pieceSquares[i] != -1; i++)
		{
			bitRef = pieceSquares[i];
			occupancy = (int)(m_pieceBitboards[RivalConstants.ROTATED45CLOCK] >>> m_bitboards.diagonalShiftAfterClockwiseRotation[bitRef]);
			occupancy &= m_bitboards.byteLengthMask[m_bitboards.diagonalLengthAfterClockwiseRotation[bitRef]];
			occupancy &= 126; // forget first and last bits
			occupancy >>>= 1; // shift to a number between 0-63
			bitboardMoves = m_bitboards.clockwiseDiagonalSlideMoves[bitRef][occupancy] & ~m_pieceBitboards[RivalConstants.FRIENDLY];
			addMoves(bitRef, bitboardMoves);

			occupancy = (int)(m_pieceBitboards[RivalConstants.ROTATED45ANTI] >> m_bitboards.diagonalShiftAfterAntiClockwiseRotation[bitRef] & m_bitboards.byteLengthMask[m_bitboards.diagonalLengthAfterAntiClockwiseRotation[bitRef]]);
			occupancy &= 126; // forget first and last bits
			occupancy >>>= 1; // shift to a number between 0-63
			bitboardMoves = m_bitboards.antiClockwiseDiagonalSlideMoves[bitRef][occupancy] & ~m_pieceBitboards[RivalConstants.FRIENDLY];
			addMoves(bitRef, bitboardMoves);
		}	
	}
	
	public void generateStraightSlidingMovesWithBitboards(long bitmapPiecesToMove)
	{
		long bitboardMoves;
		int pieceSquares[];
		int rank, file;
		int occupancy;
		
		pieceSquares = Bitboards.getSetBits(bitmapPiecesToMove);		
		for (int i=0; pieceSquares[i] != -1; i++)
		{
			rank = (int)(pieceSquares[i] / 8);
			file = 7 - (pieceSquares[i] % 8);
			occupancy = (int)(m_pieceBitboards[RivalConstants.ALL] >> (rank*8) & 255);
			occupancy &= 126; // forget first and last
			occupancy >>>= 1; // shift to a number between 0-63
			bitboardMoves = m_bitboards.horizontalSlideMoves[pieceSquares[i]][occupancy] & ~m_pieceBitboards[RivalConstants.FRIENDLY];
			addMoves(pieceSquares[i], bitboardMoves);
			
			occupancy = (int)(m_pieceBitboards[RivalConstants.ROTATED90ANTI] >> (file*8) & 255) & 126;
			occupancy &= 126; // forget first and last
			occupancy >>>= 1; // shift to a number between 0-63
			bitboardMoves = m_bitboards.verticalSlideMoves[pieceSquares[i]][occupancy] & ~m_pieceBitboards[RivalConstants.FRIENDLY];
			addMoves(pieceSquares[i], bitboardMoves);
		}		
	}
	
	public void generateKnightMoves()
	{
		int knightSquares[];
		long bitboardKnightMoves;
		
		if (this.m_isWhiteToMove)
		{
			knightSquares = Bitboards.getSetBits(m_pieceBitboards[RivalConstants.WN]);
			//knightSquares = m_pieceLocations[RivalConstants.WN];
			//knightCount = m_pieceCounters[RivalConstants.WN];
		}
		else
		{
			knightSquares = Bitboards.getSetBits(m_pieceBitboards[RivalConstants.BN]);
			//knightSquares = m_pieceLocations[RivalConstants.BN];
			//knightCount = m_pieceCounters[RivalConstants.BN];
		}
		
		for (int i=0; knightSquares[i] != -1; i++)
		{
			bitboardKnightMoves = Bitboards.knightMoves[knightSquares[i]] & ~m_pieceBitboards[RivalConstants.FRIENDLY];
			addMoves(knightSquares[i], bitboardKnightMoves);
		}
	}
	
	public boolean isCheck() 
	{
		if (m_isWhiteToMove)
		{
			return isSquareAttacked(m_whiteKingSquare, false);
		}
		else
		{
			return isSquareAttacked(m_blackKingSquare, true);
		}
	}
	
	public boolean isSquareAttacked(int bitRef, boolean isWhiteAttacking)
	{
		boolean isCheck = false;
		int i;
		
		if (!isWhiteAttacking)
		{
			isCheck = 
				(m_pieceBitboards[RivalConstants.BN] & Bitboards.knightMoves[bitRef]) != 0 |
				(m_pieceBitboards[RivalConstants.BK] & Bitboards.kingMoves[bitRef]) != 0 |
				(m_pieceBitboards[RivalConstants.BP] & Bitboards.whitePawnMovesCapture[bitRef] ) != 0;
		
			for (i=bitRef+8; i<=63 && !isCheck; i+=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BR] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}
			
			for (i=bitRef-8; i>=0 && !isCheck; i-=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BR] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}

			for (i=bitRef+1; i%8!=0 && !isCheck; i++) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BR] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}

			for (i=bitRef-1; i%8!=7 && i>=0 && !isCheck; i--) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BR] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}
			
			for (i=bitRef+9; i%8!=0 && i<=63 && !isCheck; i+=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BB] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}			

			for (i=bitRef-9; i%8!=7 && i>=0 && !isCheck; i-=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BB] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}			

			for (i=bitRef+7; i%8!=7 && i<=63 && !isCheck; i+=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BB] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}			

			for (i=bitRef-7; i%8!=0 && i>=0 && !isCheck; i-=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BB] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}
		}
		else
		{
			isCheck = 
				(m_pieceBitboards[RivalConstants.WN] & Bitboards.knightMoves[bitRef]) != 0 |
				(m_pieceBitboards[RivalConstants.WK] & Bitboards.kingMoves[bitRef]) != 0 |
				(m_pieceBitboards[RivalConstants.WP] & Bitboards.blackPawnMovesCapture[bitRef] ) != 0;
		
			for (i=bitRef+8; i<=63 && !isCheck; i+=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WR] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}
			
			for (i=bitRef-8; i>=0 && !isCheck; i-=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WR] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}

			for (i=bitRef+1; i%8!=0 && !isCheck; i++) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WR] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}

			for (i=bitRef-1; i%8!=7 && i>=0 && !isCheck; i--) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WR] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}
			
			for (i=bitRef+9; i%8!=0 && i<=63 && !isCheck; i+=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WB] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}			

			for (i=bitRef-9; i%8!=7 && i>=0 && !isCheck; i-=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WB] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}			

			for (i=bitRef+7; i%8!=7 && i<=63 && !isCheck; i+=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WB] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}			

			for (i=bitRef-7; i%8!=0 && i>=0 && !isCheck; i-=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WB] & (1L << i)) != 0) {
						isCheck = true;
					}
					break;
				}
			}			
		}
		return isCheck;
	}

	public int countAttackersWithXRays(int bitRef, boolean isWhiteAttacking)
	{
		int attackers = 0;
		int i;
		
		if (!isWhiteAttacking)
		{
			attackers += Bitboards.countSetBits(m_pieceBitboards[RivalConstants.BN] & Bitboards.knightMoves[bitRef]);
			attackers += Bitboards.countSetBits(m_pieceBitboards[RivalConstants.BK] & Bitboards.kingMoves[bitRef]);
			attackers += Bitboards.countSetBits(m_pieceBitboards[RivalConstants.BN] & Bitboards.whitePawnMovesCapture[bitRef]);
		
			for (i=bitRef+8; i<=63; i+=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BR] & (1L << i)) != 0) {
						attackers ++;
					}
					else break; // break if we encounter anything other than a black rook or queen, otherwise see if there is another rook or queen attacking
				}
			}
			
			for (i=bitRef-8; i>=0; i-=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BR] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}

			for (i=bitRef+1; i%8!=0; i++) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BR] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}

			for (i=bitRef-1; i%8!=7 && i>=0; i--) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BR] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}
			
			for (i=bitRef+9; i%8!=0 && i<=63; i+=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BB] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}			

			for (i=bitRef-9; i%8!=7 && i>=0; i-=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BB] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}			

			for (i=bitRef+7; i%8!=7 && i<=63; i+=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BB] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}			

			for (i=bitRef-7; i%8!=0 && i>=0; i-=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.BQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.BB] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}
		}
		else
		{
			attackers += Bitboards.countSetBits(m_pieceBitboards[RivalConstants.WN] & Bitboards.knightMoves[bitRef]);
			attackers += Bitboards.countSetBits(m_pieceBitboards[RivalConstants.WK] & Bitboards.kingMoves[bitRef]);
			attackers += Bitboards.countSetBits(m_pieceBitboards[RivalConstants.WN] & Bitboards.blackPawnMovesCapture[bitRef]);
		
			for (i=bitRef+8; i<=63; i+=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WR] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}
			
			for (i=bitRef-8; i>=0; i-=8) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WR] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}

			for (i=bitRef+1; i%8!=0; i++) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WR] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}

			for (i=bitRef-1; i%8!=7 && i>=0; i--) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WR] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}
			
			for (i=bitRef+9; i%8!=0 && i<=63; i+=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WB] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}			

			for (i=bitRef-9; i%8!=7 && i>=0; i-=9) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WB] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}			

			for (i=bitRef+7; i%8!=7 && i<=63; i+=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WB] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}			

			for (i=bitRef-7; i%8!=0 && i>=0; i-=7) {
				if ((m_pieceBitboards[RivalConstants.ALL] & (1L << i)) != 0) {
					if ((m_pieceBitboards[RivalConstants.WQ] & (1L << i)) != 0 || (m_pieceBitboards[RivalConstants.WB] & (1L << i)) != 0) {
						attackers ++;
					}
					else break;
				}
			}			
		}
		return attackers;
	}
	
	public void generateKingMoves()
	{
		int kingSquare;
		long bitboardKingMoves;
		
		if (this.m_isWhiteToMove)
		{
			kingSquare = this.m_whiteKingSquare;
			
			if (m_pieceBitboards[RivalConstants.WHITEKINGSIDECASTLEMASK] != 0L && (m_pieceBitboards[RivalConstants.ALL] & Bitboards.WHITEKINGSIDECASTLESQUARES) == 0L)
			{
				if (!isSquareAttacked(3,false) && !isSquareAttacked(2,false))
				{
					this.m_legalMoves[this.m_numLegalMoves++] = (3 << 16) | 1;
				}
			}
			else
			if (m_pieceBitboards[RivalConstants.WHITEQUEENSIDECASTLEMASK] != 0L && (m_pieceBitboards[RivalConstants.ALL] & Bitboards.WHITEQUEENSIDECASTLESQUARES) == 0L)
			{
				if (!isSquareAttacked(3,false) && !isSquareAttacked(4,false))
				{
					this.m_legalMoves[this.m_numLegalMoves++] = (3 << 16) | 5;
				}
			}
		}
		else
		{
			kingSquare = this.m_blackKingSquare;
			
			if (m_pieceBitboards[RivalConstants.BLACKKINGSIDECASTLEMASK] != 0L && (m_pieceBitboards[RivalConstants.ALL] & Bitboards.BLACKKINGSIDECASTLESQUARES) == 0L)
			{
				if (!isSquareAttacked(59,true) && !isSquareAttacked(58,true))
				{
					this.m_legalMoves[this.m_numLegalMoves++] = (59 << 16) | 57;
				}
			}
			else
			if (m_pieceBitboards[RivalConstants.BLACKQUEENSIDECASTLEMASK] != 0L && (m_pieceBitboards[RivalConstants.ALL] & Bitboards.BLACKQUEENSIDECASTLESQUARES) == 0L)
			{
				if (!isSquareAttacked(59,true) && !isSquareAttacked(60,true))
				{
					this.m_legalMoves[this.m_numLegalMoves++] = (59 << 16) | 61;
				}
			}
		}
		
		bitboardKingMoves = Bitboards.kingMoves[kingSquare] & ~m_pieceBitboards[RivalConstants.FRIENDLY];
		addMoves(kingSquare, bitboardKingMoves);
	}
	
	public void generatePawnMoves() 
	{
		if (this.m_isWhiteToMove)
		{
			pawnSquares = Bitboards.getSetBits(m_pieceBitboards[RivalConstants.WP]);
			bitboardMaskForward = Bitboards.whitePawnMovesForward;
			bitboardMaskCapture = Bitboards.whitePawnMovesCapture;
		}
		else
		{
			pawnSquares = Bitboards.getSetBits(m_pieceBitboards[RivalConstants.BP]);
			bitboardMaskForward = Bitboards.blackPawnMovesForward;
			bitboardMaskCapture = Bitboards.blackPawnMovesCapture;
		}
		
		for (int i=0; pawnSquares[i] != -1; i++)
		{
			bitboardPawnMoves = bitboardMaskForward[pawnSquares[i]] & ~m_pieceBitboards[RivalConstants.ALL];
			
			if (this.m_isWhiteToMove)
			{
				bitboardJumpMoves = bitboardPawnMoves << 8L; // if we can move one, maybe we can move two
				bitboardJumpMoves &= Bitboards.RANK_4; // only counts if move is to fourth rank 
			}
			else
			{
				bitboardJumpMoves = bitboardPawnMoves >> 8L;
				bitboardJumpMoves &= Bitboards.RANK_5;
			}
			bitboardJumpMoves &= ~m_pieceBitboards[RivalConstants.ALL]; // only if square empty
			bitboardPawnMoves |= bitboardJumpMoves;
			
			bitboardPawnMoves |= bitboardMaskCapture[pawnSquares[i]] & m_pieceBitboards[RivalConstants.ENEMY];
			bitboardPawnMoves |= bitboardMaskCapture[pawnSquares[i]] & m_pieceBitboards[RivalConstants.ENPASSANTSQUARE];

			addPossiblePromotionMoves(pawnSquares[i], bitboardPawnMoves);
		}
	}
	
	private void addPossiblePromotionMoves(int fromSquare, long bitboard)
	{
		int toSquare;
		while (bitboard != 0)
		{
			if ((bitboard & 0xffff000000000000L) != 0) toSquare = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
			if ((bitboard & 0x0000ffff00000000L) != 0) toSquare = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
			if ((bitboard & 0x00000000ffff0000L) != 0) toSquare = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
			toSquare = Bitboards.firstBit16[(int)(bitboard)];
			
			bitboard ^= (1L << toSquare);
			
			if (toSquare>=56 || toSquare<=7)
			{
				this.m_legalMoves[this.m_numLegalMoves++] = (fromSquare << 16) | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT);
				this.m_legalMoves[this.m_numLegalMoves++] = (fromSquare << 16) | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP);
				this.m_legalMoves[this.m_numLegalMoves++] = (fromSquare << 16) | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN);
				this.m_legalMoves[this.m_numLegalMoves++] = (fromSquare << 16) | toSquare | (RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK);
			}
			else
			{
				this.m_legalMoves[this.m_numLegalMoves++] = (fromSquare << 16) | toSquare;
			}
		}
	}

	private void addMoves(int fromSquare, long bitboard)
	{
		int toSquare;
		while (bitboard != 0)
		{
			if ((bitboard & 0xffff000000000000L) != 0) toSquare = Bitboards.firstBit16[(int)(bitboard >>> 48L)] + 48; else
			if ((bitboard & 0x0000ffff00000000L) != 0) toSquare = Bitboards.firstBit16[(int)(bitboard >>> 32L)] + 32; else
			if ((bitboard & 0x00000000ffff0000L) != 0) toSquare = Bitboards.firstBit16[(int)(bitboard >>> 16L)] + 16; else
			toSquare = Bitboards.firstBit16[(int)(bitboard)];
			bitboard ^= (1L << toSquare);
			
			this.m_legalMoves[this.m_numLegalMoves++] = (fromSquare << 16) | toSquare;
		}
	}
	
	public void generateLegalMoves()
	{
		resetLegalMoves();

//		if (this.m_isWhiteToMove)
//		{
//			generateStraightSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.WR]);
//			generateStraightSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.WQ]);
//			int n = this.m_numLegalMoves;
//
//			resetLegalMoves();
//
//			generateStraightSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.WR]);
//			generateStraightSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.WQ]);
//			
//			if (n != this.m_numLegalMoves)
//			{
//				printMoveList(this.m_legalMoves);
//				printBoard();
//				System.exit(0);
//			}
//			
//			resetLegalMoves();
//		}
		
		generateKnightMoves();
		generateKingMoves();
		generatePawnMoves();
		if (this.m_isWhiteToMove)
		{
			if (RivalConstants.GENERATE_SLIDING_MOVES_WITH_LOOPS)
			{
				generateStraightSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.WR]);
				generateStraightSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.WQ]);
				generateDiagonalSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.WB]);
				generateDiagonalSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.WQ]);
			}
			else
			{
				generateStraightSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.WR]);
				generateStraightSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.WQ]);
				generateDiagonalSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.WB]);
				generateDiagonalSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.WQ]);
			}
		}
		else
		{
			if (RivalConstants.GENERATE_SLIDING_MOVES_WITH_LOOPS)
			{
				generateStraightSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.BR]);
				generateStraightSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.BQ]);
				generateDiagonalSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.BB]);
				generateDiagonalSlidingMovesWithLoops(m_pieceBitboards[RivalConstants.BQ]);
			}
			else
			{
				generateStraightSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.BR]);
				generateStraightSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.BQ]);
				generateDiagonalSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.BB]);
				generateDiagonalSlidingMovesWithBitboards(m_pieceBitboards[RivalConstants.BQ]);
			}
		}

		this.m_legalMoves[this.m_numLegalMoves] = 0;
	}

	public void setLegalMoves( int[] moveArray )
	{
		this.m_legalMoves = moveArray;
		generateLegalMoves();
	}
	
	public void setBitboards(BoardModel board)
	{
		byte bitNum;
		long bitSet;
		int pieceIndex = -1;
		char piece;
		
		this.m_pieceBitboards = new long[RivalConstants.NUM_BITBOARDS];
		this.m_pieceLocations = new int[12][9];
		this.m_pieceCounters = new int[12];
		
		for (int i=RivalConstants.WP; i<=RivalConstants.BR; i++)
		{
			m_pieceBitboards[i] = 0;
			m_pieceLocations[i][0] = -1;
			m_pieceCounters[i] = 0;
		}
		
		for (int y=0; y<8; y++)
		{
			for (int x=0; x<8; x++)
			{
				bitNum = (byte)(63 - (8*y) - x);
				bitSet = 1L << bitNum;
				piece = board.getPieceCode(x, y);
				switch (piece)
				{
					case 'P' : pieceIndex = RivalConstants.WP; break; 
					case 'p' : pieceIndex = RivalConstants.BP; break;
					case 'N' : pieceIndex = RivalConstants.WN; break; 
					case 'n' : pieceIndex = RivalConstants.BN; break; 
					case 'B' : pieceIndex = RivalConstants.WB; break; 
					case 'b' : pieceIndex = RivalConstants.BB; break; 
					case 'R' : pieceIndex = RivalConstants.WR; break; 
					case 'r' : pieceIndex = RivalConstants.BR; break; 
					case 'Q' : pieceIndex = RivalConstants.WQ; break; 
					case 'q' : pieceIndex = RivalConstants.BQ; break; 
					case 'K' : pieceIndex = RivalConstants.WK; this.m_whiteKingSquare = bitNum; break; 
					case 'k' : pieceIndex = RivalConstants.BK; this.m_blackKingSquare = bitNum; break;
					default  : pieceIndex = -1;
				}
				if (pieceIndex != -1)
				{
					m_pieceBitboards[pieceIndex] = m_pieceBitboards[pieceIndex] | bitSet;
					
					if (RivalConstants.TRACK_PIECE_LOCATIONS)
					{
						m_pieceLocations[pieceIndex][m_pieceCounters[pieceIndex]] = bitNum;
						m_pieceLocations[pieceIndex][m_pieceCounters[pieceIndex]+1] = -1;
						m_pieceCounters[pieceIndex]++;
					}
				}
			}
		}
		
		this.m_isWhiteToMove = board.isWhiteToMove();
		
		int ep = board.getEnPassantFile();
		if (ep == -1)
		{
			ep = 8;
			m_pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 0; 
		}
		else
		{
			if (board.isWhiteToMove())
			{
				m_pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 1L << (40 + (7-ep));
			}
			else
			{
				m_pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 1L << (16 + (7-ep));
			}
		}
		
		this.m_pieceBitboards[RivalConstants.WHITEKINGSIDECASTLEMASK] = (board.isWhiteKingSideCastleAvailable() ? Bitboards.WHITEKINGSIDECASTLESQUARES : 0);
		this.m_pieceBitboards[RivalConstants.WHITEQUEENSIDECASTLEMASK] = (board.isWhiteQueenSideCastleAvailable() ? Bitboards.WHITEQUEENSIDECASTLESQUARES : 0);
		this.m_pieceBitboards[RivalConstants.BLACKKINGSIDECASTLEMASK] = (board.isBlackKingSideCastleAvailable() ? Bitboards.BLACKKINGSIDECASTLESQUARES : 0);
		this.m_pieceBitboards[RivalConstants.BLACKQUEENSIDECASTLEMASK] = (board.isBlackQueenSideCastleAvailable() ? Bitboards.BLACKQUEENSIDECASTLESQUARES : 0);
		
		calculateSupplementaryBitboards();
	}

	public void calculateSupplementaryBitboards() 
	{
		m_pieceBitboards[RivalConstants.ALLWHITE] = 
			m_pieceBitboards[RivalConstants.WP] | m_pieceBitboards[RivalConstants.WN] | 
			m_pieceBitboards[RivalConstants.WB] | m_pieceBitboards[RivalConstants.WQ] | 
			m_pieceBitboards[RivalConstants.WK] | m_pieceBitboards[RivalConstants.WR];

		m_pieceBitboards[RivalConstants.ALLBLACK] = 
			m_pieceBitboards[RivalConstants.BP] | m_pieceBitboards[RivalConstants.BN] | 
			m_pieceBitboards[RivalConstants.BB] | m_pieceBitboards[RivalConstants.BQ] | 
			m_pieceBitboards[RivalConstants.BK] | m_pieceBitboards[RivalConstants.BR];
		
		if (this.m_isWhiteToMove)
		{
			m_pieceBitboards[RivalConstants.FRIENDLY] = m_pieceBitboards[RivalConstants.ALLWHITE];
			m_pieceBitboards[RivalConstants.ENEMY] = m_pieceBitboards[RivalConstants.ALLBLACK];
		}
		else
		{
			m_pieceBitboards[RivalConstants.FRIENDLY] = m_pieceBitboards[RivalConstants.ALLBLACK];
			m_pieceBitboards[RivalConstants.ENEMY] = m_pieceBitboards[RivalConstants.ALLWHITE];
		}
		
		this.m_pieceBitboards[RivalConstants.ALL] = m_pieceBitboards[RivalConstants.FRIENDLY] | m_pieceBitboards[RivalConstants.ENEMY];
		
		if (!RivalConstants.GENERATE_SLIDING_MOVES_WITH_LOOPS)
		{
			this.m_pieceBitboards[RivalConstants.ROTATED90ANTI] = BitwiseOperation.rotateBitboard90AntiClockwise(m_pieceBitboards[RivalConstants.ALL]);
			this.m_pieceBitboards[RivalConstants.ROTATED45CLOCK] = BitwiseOperation.rotateBitboard45ClockwiseFast(m_pieceBitboards[RivalConstants.ALL]);
			this.m_pieceBitboards[RivalConstants.ROTATED45ANTI] = BitwiseOperation.rotateBitboard45AntiClockwiseFast(m_pieceBitboards[RivalConstants.ALL]);
		}
	}
	
	public void addPieceLocation(int pieceIndex, int sq)
	{
		m_pieceLocations[pieceIndex][m_pieceCounters[pieceIndex]] = sq;
		m_pieceLocations[pieceIndex][m_pieceCounters[pieceIndex]+1] = -1;
		m_pieceCounters[pieceIndex] ++;
	}

	public void updatePieceLocation(int pieceIndex, int from, int to)
	{
		for (int i=0; m_pieceLocations[pieceIndex][i] != -1; i++)
		{
			if (m_pieceLocations[pieceIndex][i] == from)
			{
				m_pieceLocations[pieceIndex][i] = to;
				break;
			}
		}
	}

	public void removePieceLocation(int pieceIndex, int from)
	{
		boolean found = false;
		for (int i=0; m_pieceLocations[pieceIndex][i] != -1; i++)
		{
			if (m_pieceLocations[pieceIndex][i] == from)
			{
				found = true;
			}
			if (found)
			{
				m_pieceLocations[pieceIndex][i] = m_pieceLocations[pieceIndex][i+1];
			}
		}
		m_pieceCounters[pieceIndex] --;
	}
	
	public void resetBackups()
	{
		this.m_numBackups = 0;
	}
	
	public boolean isOnNullMove()
	{
		return this.m_isOnNullMove;
	}
	
	public void unMakeNullMove() 
	{
		makeNullMove();
		
		this.m_isOnNullMove = false;
	}

	public void makeNullMove() 
	{
		this.m_isWhiteToMove = !this.m_isWhiteToMove;
		
		long t = this.m_pieceBitboards[RivalConstants.FRIENDLY];
		this.m_pieceBitboards[RivalConstants.FRIENDLY] = this.m_pieceBitboards[RivalConstants.ENEMY];
		this.m_pieceBitboards[RivalConstants.ENEMY] = t;
		
		this.m_hashValue ^= this.m_hashSwitchMovers;
		
		this.m_isOnNullMove = true;
	}

	public void unMakeMove()
	{
		this.m_numBackups --;
		
		this.m_whiteKingSquare = this.m_whiteKingSquareBackup[this.m_numBackups];
		this.m_blackKingSquare = this.m_blackKingSquareBackup[this.m_numBackups];
		this.m_whiteHasCastled = this.m_whiteHasCastledBackup[this.m_numBackups];
		this.m_blackHasCastled = this.m_blackHasCastledBackup[this.m_numBackups];
		this.m_hashValue = this.m_hashValueBackup[this.m_numBackups];
		this.m_whitePawnHashValue = this.m_whitePawnHashValueBackup[this.m_numBackups];
		this.m_blackPawnHashValue = this.m_blackPawnHashValueBackup[this.m_numBackups];

		this.m_isWhiteToMove = this.m_isWhiteToMoveBackup[this.m_numBackups];

		for (int i=0; i<RivalConstants.NUM_BITBOARDS; i++)
		{
			this.m_pieceBitboards[i] = this.m_pieceBitboardsBackup[this.m_numBackups][i];
		}

		if (RivalConstants.TRACK_PIECE_LOCATIONS)
		{
			for (int i=RivalConstants.WP; i<=RivalConstants.BR; i++)
			{
				this.m_pieceCounters[i] = this.m_pieceCountersBackup[this.m_numBackups][i];
				for (int j=0; j<this.m_pieceCounters[i]+1; j++)
				{
					this.m_pieceLocations[i][j] = this.m_pieceLocationsBackup[this.m_numBackups][i][j];
				}
			}
		}
	}
	
	public void makeMove(int compactMove) 
	{
		//if (compactMove == 655362 && this.m_numBackups == 0) System.exit(0);
		byte moveFrom = (byte)(compactMove >>> 16);
		byte moveTo = (byte)((compactMove & 511) & 63);
		
		long fromMask = 1L << moveFrom;
		long toMask = 1L << moveTo;

		this.m_whiteKingSquareBackup[this.m_numBackups] = this.m_whiteKingSquare;
		this.m_blackKingSquareBackup[this.m_numBackups] = this.m_blackKingSquare;
		this.m_whiteHasCastledBackup[this.m_numBackups] = this.m_whiteHasCastled;
		this.m_blackHasCastledBackup[this.m_numBackups] = this.m_blackHasCastled;
		this.m_isWhiteToMoveBackup[this.m_numBackups] = this.m_isWhiteToMove;
		this.m_hashValueBackup[this.m_numBackups] = this.m_hashValue;
		this.m_blackPawnHashValueBackup[this.m_numBackups] = this.m_blackPawnHashValue;
		this.m_whitePawnHashValueBackup[this.m_numBackups] = this.m_whitePawnHashValue;
		this.m_moveList[this.m_numBackups] = compactMove; 
		
		for (int i=0; i<RivalConstants.NUM_BITBOARDS; i++)
		{
			this.m_pieceBitboardsBackup[this.m_numBackups][i] = this.m_pieceBitboards[i];
		}

		if (RivalConstants.TRACK_PIECE_LOCATIONS)
		{
			for (int i=RivalConstants.WP; i<=RivalConstants.BR; i++)
			{
				this.m_pieceCountersBackup[this.m_numBackups][i] = this.m_pieceCounters[i];
				for (int j=0; j<this.m_pieceCounters[i]+1; j++)
				{
					this.m_pieceLocationsBackup[this.m_numBackups][i][j] = this.m_pieceLocations[i][j];
				}
			}
		}
		
		this.m_numBackups ++;
		this.m_moveList[this.m_numBackups+1] = 0; 
		
		long currentEnPassantSquare = this.m_pieceBitboards[RivalConstants.ENPASSANTSQUARE]; 
		this.m_pieceBitboards[RivalConstants.ENPASSANTSQUARE] = 0;
		
		if (this.m_isWhiteToMove)
		{
			for (int i=RivalConstants.WP; i<=RivalConstants.WR; i++)
			{
				if ((this.m_pieceBitboards[i] & fromMask) != 0) 
				{ 
					this.m_pieceBitboards[i] ^= fromMask | toMask;
					this.m_hashValue ^= this.m_pieceHashValues[i][moveFrom] ^ this.m_pieceHashValues[i][moveTo];
					
					//updatePieceLocation(i, moveFrom, moveTo);
					
					if (i == RivalConstants.WK)
					{
						this.m_whiteKingSquare = moveTo;
						this.m_pieceBitboards[RivalConstants.WHITEKINGSIDECASTLEMASK] = 0L;
						this.m_pieceBitboards[RivalConstants.WHITEQUEENSIDECASTLEMASK] = 0L;
						if ((toMask | fromMask) == Bitboards.WHITEKINGSIDECASTLEMOVEMASK)
						{
							this.m_pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEKINGSIDECASTLEROOKMOVE;
							this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.WR][0] ^ this.m_pieceHashValues[RivalConstants.WR][2];
							this.m_whiteHasCastled = true;
							//updatePieceLocation(RivalConstants.WR, 0, 2);

						}
						if ((toMask | fromMask) == Bitboards.WHITEQUEENSIDECASTLEMOVEMASK)
						{
							this.m_pieceBitboards[RivalConstants.WR] ^= Bitboards.WHITEQUEENSIDECASTLEROOKMOVE;
							this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.WR][4] ^ this.m_pieceHashValues[RivalConstants.WR][7];
							this.m_whiteHasCastled = true;
							//updatePieceLocation(RivalConstants.WR, 4, 7);
						}
					}
					else
					if (i == RivalConstants.WP)
					{
						this.m_whitePawnHashValue ^= this.m_pieceHashValues[RivalConstants.WP][moveFrom] ^ this.m_pieceHashValues[RivalConstants.WP][moveTo];
						
						if ((toMask & Bitboards.RANK_4) != 0 && (fromMask & Bitboards.RANK_2) != 0)
						{
							this.m_pieceBitboards[RivalConstants.ENPASSANTSQUARE] = fromMask << 8L;
						}
						else
						if (toMask == currentEnPassantSquare)
						{
							this.m_pieceBitboards[RivalConstants.BP] ^= toMask >>> 8;
							this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.BP][moveTo-8];
							this.m_blackPawnHashValue ^= this.m_pieceHashValues[RivalConstants.BP][moveTo-8];
							//removePieceLocation(RivalConstants.BP, moveTo-8);
						}
						else
						if ((toMask & Bitboards.RANK_8) != 0)
						{
							int promotionPieceCode = compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL;
							switch (promotionPieceCode)
							{
								case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN : 
									this.m_pieceBitboards[RivalConstants.WQ] |= toMask;
									this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.WQ][moveTo];
									//addPieceLocation(RivalConstants.WQ, moveTo);
									break;
								case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK : 
									this.m_pieceBitboards[RivalConstants.WR] |= toMask;
									this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.WR][moveTo];
									//addPieceLocation(RivalConstants.WR, moveTo);
									break;
								case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT : 
									this.m_pieceBitboards[RivalConstants.WN] |= toMask;
									this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.WN][moveTo];
									//addPieceLocation(RivalConstants.WN, moveTo);
									break;
								case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP : 
									this.m_pieceBitboards[RivalConstants.WB] |= toMask;
									this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.WB][moveTo];									
									//addPieceLocation(RivalConstants.WB, moveTo);
									break;
							}
							this.m_pieceBitboards[RivalConstants.WP] ^= toMask;
							//removePieceLocation(RivalConstants.WP, moveTo);
							this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.WP][moveTo];
							this.m_whitePawnHashValue ^= this.m_pieceHashValues[RivalConstants.WP][moveTo];
						}
					}
					else
					if (i == RivalConstants.WR)
					{
						if (fromMask == Bitboards.WHITEQUEENSIDEROOKMASK) this.m_pieceBitboards[RivalConstants.WHITEQUEENSIDECASTLEMASK] = 0L; else
						if (fromMask == Bitboards.WHITEKINGSIDEROOKMASK) this.m_pieceBitboards[RivalConstants.WHITEKINGSIDECASTLEMASK] = 0L;
					}
					break;
				}
			}
			
			for (int i=RivalConstants.BP; i<=RivalConstants.BR; i++)
			{
				if ((this.m_pieceBitboards[i] & toMask) != 0) 
				{ 
					this.m_pieceBitboards[i] ^= toMask;
					//removePieceLocation(i, moveTo);
					this.m_hashValue ^= this.m_pieceHashValues[i][moveTo];
					if (i==RivalConstants.BP)
					{
						this.m_blackPawnHashValue ^= this.m_pieceHashValues[i][moveTo];
					}
					else
					if (i==RivalConstants.BR)
					{
						if (toMask == Bitboards.BLACKKINGSIDEROOKMASK) this.m_pieceBitboards[RivalConstants.BLACKKINGSIDECASTLEMASK] = 0L; else
						if (toMask == Bitboards.BLACKQUEENSIDEROOKMASK) this.m_pieceBitboards[RivalConstants.BLACKQUEENSIDECASTLEMASK] = 0L;
					}
					break;
				}
			}
		}
		else
		{
			for (int i=RivalConstants.BP; i<=RivalConstants.BR; i++)
			{
				if ((this.m_pieceBitboards[i] & fromMask) != 0) 
				{ 
					this.m_pieceBitboards[i] ^= fromMask | toMask;
					this.m_hashValue ^= this.m_pieceHashValues[i][moveFrom] ^ this.m_pieceHashValues[i][moveTo];
					
					//updatePieceLocation(i, moveFrom, moveTo);
					
					if (i == RivalConstants.BK)
					{
						this.m_pieceBitboards[RivalConstants.BLACKKINGSIDECASTLEMASK] = 0L;
						this.m_pieceBitboards[RivalConstants.BLACKQUEENSIDECASTLEMASK] = 0L;
						this.m_blackKingSquare = moveTo;
						if ((toMask | fromMask) == Bitboards.BLACKKINGSIDECASTLEMOVEMASK)
						{
							this.m_pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKKINGSIDECASTLEROOKMOVE;
							this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.BR][56] ^ this.m_pieceHashValues[RivalConstants.BR][58];
							this.m_blackHasCastled = true;
							//updatePieceLocation(RivalConstants.BR, 56, 58);
						}
						if ((toMask | fromMask) == Bitboards.BLACKQUEENSIDECASTLEMOVEMASK)
						{
							this.m_pieceBitboards[RivalConstants.BR] ^= Bitboards.BLACKQUEENSIDECASTLEROOKMOVE;
							this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.BR][63] ^ this.m_pieceHashValues[RivalConstants.BR][60];
							this.m_blackHasCastled = true;
							//updatePieceLocation(RivalConstants.BR, 63, 60);
						}
					}
					else
					if (i == RivalConstants.BP)
					{
						this.m_blackPawnHashValue ^= this.m_pieceHashValues[RivalConstants.BP][moveFrom] ^ this.m_pieceHashValues[RivalConstants.BP][moveTo];
						
						if ((toMask & Bitboards.RANK_5) != 0 && (fromMask & Bitboards.RANK_7) != 0)
						{
							this.m_pieceBitboards[RivalConstants.ENPASSANTSQUARE] = toMask << 8L;
						}
						else
						if (toMask == currentEnPassantSquare)
						{
							this.m_pieceBitboards[RivalConstants.WP] ^= toMask << 8;
							this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.WP][moveTo+8];
							this.m_whitePawnHashValue ^= this.m_pieceHashValues[RivalConstants.WP][moveTo+8];
							//removePieceLocation(RivalConstants.WP, moveTo+8);
						}
						else
						if ((toMask & Bitboards.RANK_1) != 0)
						{
							int promotionPieceCode = compactMove & RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_FULL;
							switch (promotionPieceCode)
							{
								case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_QUEEN : 
									this.m_pieceBitboards[RivalConstants.BQ] |= toMask;
									this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.BQ][moveTo];
									//addPieceLocation(RivalConstants.BQ, moveTo);
									break;
								case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_ROOK : 
									this.m_pieceBitboards[RivalConstants.BR] |= toMask;
									this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.BR][moveTo];
									//addPieceLocation(RivalConstants.BR, moveTo);
									break;
								case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_KNIGHT : 
									this.m_pieceBitboards[RivalConstants.BN] |= toMask;
									this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.BN][moveTo];
									//addPieceLocation(RivalConstants.BN, moveTo);
									break;
								case RivalConstants.PROMOTION_PIECE_TOSQUARE_MASK_BISHOP : 
									this.m_pieceBitboards[RivalConstants.BB] |= toMask;
									this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.BB][moveTo];
									//addPieceLocation(RivalConstants.BB, moveTo);
									break;
							}
							this.m_pieceBitboards[RivalConstants.BP] ^= toMask;
							//removePieceLocation(RivalConstants.BP, moveTo);
							this.m_hashValue ^= this.m_pieceHashValues[RivalConstants.BP][moveTo];
							this.m_blackPawnHashValue ^= this.m_pieceHashValues[RivalConstants.BP][moveTo];
						}
					}
					else
					if (i == RivalConstants.BR)
					{
						if (fromMask == Bitboards.BLACKQUEENSIDEROOKMASK || toMask == Bitboards.BLACKQUEENSIDEROOKMASK) this.m_pieceBitboards[RivalConstants.BLACKQUEENSIDECASTLEMASK] = 0L; else
						if (fromMask == Bitboards.BLACKKINGSIDEROOKMASK || toMask == Bitboards.BLACKKINGSIDEROOKMASK) this.m_pieceBitboards[RivalConstants.BLACKKINGSIDECASTLEMASK] = 0L;
					}
					break;
				}
			}
			
			for (int i=RivalConstants.WP; i<=RivalConstants.WR; i++)
			{
				if ((this.m_pieceBitboards[i] & toMask) != 0) 
				{ 
					this.m_pieceBitboards[i] ^= toMask; 
					this.m_hashValue ^= this.m_pieceHashValues[i][moveTo];
					//removePieceLocation(i, moveTo);
					if (i==RivalConstants.WP)
					{
						this.m_whitePawnHashValue ^= this.m_pieceHashValues[i][moveTo];
					}
					else
					if (i==RivalConstants.WR)
					{
						if (toMask == Bitboards.WHITEKINGSIDEROOKMASK) this.m_pieceBitboards[RivalConstants.WHITEKINGSIDECASTLEMASK] = 0L; else
						if (toMask == Bitboards.WHITEQUEENSIDEROOKMASK) this.m_pieceBitboards[RivalConstants.WHITEQUEENSIDECASTLEMASK] = 0L;
					}
					break;
				}
			}
		}

		this.m_isWhiteToMove = !this.m_isWhiteToMove;
		this.m_hashValue ^= m_hashSwitchMovers;
		
		calculateSupplementaryBitboards();
	}

	public boolean isNonMoverInCheck(int[] moveArray) 
	{
		boolean nonMoverInCheck = false;
		
		this.setLegalMoves(moveArray);
		for (int i=0; m_legalMoves[i] != 0; i++)
		{
			if (this.m_isWhiteToMove)
			{
				if ((m_legalMoves[i] & 63) == this.m_blackKingSquare)
				{
					return true;
				}
			}
			else
			{
				if ((m_legalMoves[i] & 63) == this.m_whiteKingSquare)
				{
					return true;
				}
			}
		}

		return nonMoverInCheck;
	}
	
	public boolean isQuiesceMove(int[] move) 
	{
		boolean isQuiesceMove = false;
		long currentEnemyPieces = this.m_pieceBitboards[RivalConstants.ENEMY];
		this.makeMove((move[0] << 16) | move[1]);
		isQuiesceMove = currentEnemyPieces != this.m_pieceBitboards[RivalConstants.FRIENDLY];
		this.unMakeMove();
		return isQuiesceMove;
	}
	
	public int movesMade()
	{
		return this.m_numBackups;
	}

	public int captureSquareAndPiece(int movesAgo) 
	{
		int retVal = -1;

		if (this.m_pieceBitboards[RivalConstants.FRIENDLY] != this.m_pieceBitboardsBackup[this.m_numBackups-movesAgo][RivalConstants.ENEMY])
		{
			retVal = (this.m_moveList[this.m_numBackups-movesAgo] & 63); // destination square of last move
			
			// if white to move then the piece captured on previous move was white, alternate for each move back
			int offset = this.m_isWhiteToMove ? (movesAgo % 2 == 1 ? 0 : 6) : (movesAgo % 2 == 1 ? 6 : 0);
			
			for (int i=RivalConstants.WP + offset; i<=RivalConstants.WR + offset; i++)
			{
				if ((this.m_pieceBitboardsBackup[this.m_numBackups-movesAgo][i] & (1L << retVal)) != 0)
				{
					//System.out.println(allMovesString());
					retVal |= ((i-offset) << 8);
					break;
				}
			}
		}
		
		return retVal;
	}
	
	public boolean wasQuiesceMoveAndMoverNotInCheck(int[] moveArray) 
	{
		// if currently friendly pieces not equal to last backup of enemy pieces then the last move was a capture
		boolean wasQuiesce = 
				this.m_pieceBitboards[RivalConstants.FRIENDLY] != 
					this.m_pieceBitboardsBackup[this.m_numBackups-1][RivalConstants.ENEMY];
		
		boolean moverNotInCheck = true;
		
		if (wasQuiesce)
		{
			this.setLegalMoves(moveArray);
			for (int i=0; m_legalMoves[i] != 0; i++)
			{
				if (this.m_isWhiteToMove)
				{
					if ((m_legalMoves[i] & 63) == this.m_blackKingSquare)
					{
						moverNotInCheck = false;
						break;
					}
				}
				else
				{
					if ((m_legalMoves[i] & 63) == this.m_whiteKingSquare)
					{
						moverNotInCheck = false;
						break;
					}
				}
			}
		}

		return wasQuiesce && moverNotInCheck;		
	}
	
	public int previousOccurencesOfThisPosition()
	{
		int occurences = 0;
		for (int i=0; i<this.m_numBackups; i++)
		{
			if (this.m_hashValueBackup[i] == this.m_hashValue)
			{
				occurences ++;
			}
		}
		
		return occurences;
	}
	
	public String allMovesString()
	{
		String s = "";
		for (int i=0; i<this.m_numBackups && this.m_moveList[i] != 0; i++)
		{
			s += ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(this.m_moveList[i]) + " ";
		}
		
		return s;
	}
	
	public String getFen()
	{
		return getFen(this.m_numBackups);
	}
	
	public String getFen(int boardNum)
	{
		String fen = "";
		char board[] = new char[64];
		char pieces[] = new char[] {'P','N','B','Q','K','R','p','n','b','q','k','r'};
		
		for (int i=RivalConstants.WP; i<=RivalConstants.BR; i++)
		{
			int[] bitsSet = Bitboards.getSetBits((boardNum == this.m_numBackups ? m_pieceBitboards[i] : m_pieceBitboardsBackup[boardNum][i]));
			for (int j=0; bitsSet[j] != -1; j++)
			{
				board[bitsSet[j]] = pieces[i];
			}
		}
		
		char spaces = '0';
		for (int i=63; i>=0; i--)
		{
			if (board[i] == 0)
			{
				spaces ++;
			}
			else
			{
				if (spaces > '0')
				{
					fen += spaces;
					spaces = '0';
				}
				fen += board[i];
			}
			if (i % 8 == 0)
			{
				if (spaces > '0')
				{
					fen += spaces;
					spaces = '0';
				}
				if (i > 0)
				{
					fen += '/';
				}
			}
		}
		
		fen += ' ';
		fen += ((boardNum == this.m_numBackups ? m_isWhiteToMove : m_isWhiteToMoveBackup[boardNum]) ? 'w' : 'b');
		fen += ' ';
		
		fen += (boardNum == this.m_numBackups ? m_pieceBitboards[RivalConstants.WHITEKINGSIDECASTLEMASK] : m_pieceBitboardsBackup[boardNum][RivalConstants.WHITEKINGSIDECASTLEMASK]) == 0 ? "" : 'K';
		fen += (boardNum == this.m_numBackups ? m_pieceBitboards[RivalConstants.WHITEQUEENSIDECASTLEMASK] : m_pieceBitboardsBackup[boardNum][RivalConstants.WHITEQUEENSIDECASTLEMASK]) == 0 ? "" : 'Q';
		fen += (boardNum == this.m_numBackups ? m_pieceBitboards[RivalConstants.BLACKKINGSIDECASTLEMASK] : m_pieceBitboardsBackup[boardNum][RivalConstants.BLACKKINGSIDECASTLEMASK]) == 0 ? "" : 'k';
		fen += (boardNum == this.m_numBackups ? m_pieceBitboards[RivalConstants.BLACKQUEENSIDECASTLEMASK] : m_pieceBitboardsBackup[boardNum][RivalConstants.BLACKQUEENSIDECASTLEMASK]) == 0 ? "" : 'q';
		
		if ((boardNum == this.m_numBackups ? m_pieceBitboards[RivalConstants.WHITEKINGSIDECASTLEMASK] : m_pieceBitboardsBackup[boardNum][RivalConstants.WHITEKINGSIDECASTLEMASK]) + 
				(boardNum == this.m_numBackups ? m_pieceBitboards[RivalConstants.WHITEQUEENSIDECASTLEMASK] : m_pieceBitboardsBackup[boardNum][RivalConstants.WHITEQUEENSIDECASTLEMASK]) +
				(boardNum == this.m_numBackups ? m_pieceBitboards[RivalConstants.BLACKKINGSIDECASTLEMASK] : m_pieceBitboardsBackup[boardNum][RivalConstants.BLACKKINGSIDECASTLEMASK]) +
				(boardNum == this.m_numBackups ? m_pieceBitboards[RivalConstants.BLACKQUEENSIDECASTLEMASK] : m_pieceBitboardsBackup[boardNum][RivalConstants.BLACKQUEENSIDECASTLEMASK]) == 0)
		{
			fen += '-';
		}
		
		int[] ep = Bitboards.getSetBits((boardNum == this.m_numBackups ? m_pieceBitboards[RivalConstants.ENPASSANTSQUARE] : m_pieceBitboardsBackup[boardNum][RivalConstants.ENPASSANTSQUARE]));
		fen += ' ';
		if (ep[0] != -1)
		{
			char file = (char)(7 - (ep[0] % 8));
			char rank = (char)(ep[0] <= 23 ? 2 : 5);
			fen += (char)(file + 'a');
			fen += (char)(rank + '1'); 
		}
		else
		{
			fen += '-';
		}
		
		return fen;
	}
	
	public boolean isMoveLegal(int moveToVerify)
	{
		int[] moves = new int[RivalConstants.MAX_GAME_MOVES];
		int[] dummy = new int[RivalConstants.MAX_GAME_MOVES];
		this.setLegalMoves(moves);
		int i=0;
		while (moves[i] != 0)
		{
			this.makeMove(moves[i]);
			if (!this.isNonMoverInCheck(dummy))
			{
				if (moves[i] == moveToVerify) return true;
			}
			this.unMakeMove();
			i++;
		}
		return false;
	}
	
	public boolean isHanging(int square)
	{
		boolean isHanging = false;
		int defenders = this.countAttackersWithXRays(square, !this.m_isWhiteToMove);
		int attackers = this.countAttackersWithXRays(square, this.m_isWhiteToMove);
		isHanging = attackers > defenders;
		return isHanging;
	}

	public void printMoveList(int[] moves)
	{
		String s = "";
		int i=0;
		while (moves[i] != 0)
		{
			s += ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(moves[i]) + " ";
			i++;
		}
		System.out.println(s);
	}
	
	public void printLegalMoves()
	{
		String s = "";
		int[] moves = new int[RivalConstants.MAX_GAME_MOVES];
		int[] dummy = new int[RivalConstants.MAX_GAME_MOVES];
		this.setLegalMoves(moves);
		int i=0;
		while (moves[i] != 0)
		{
			this.makeMove(moves[i]);
			if (!this.isNonMoverInCheck(dummy))
			{
				s += ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(moves[i]) + " ";
			}
			this.unMakeMove();
			i++;
		}
		this.printBoard();
		System.out.println("******************** LEGAL MOVES ********************");
		System.out.println(s);
		System.out.println("*****************************************************");
	}
	
	public void printPreviousMoves()
	{
		System.out.println(this.allMovesString());
	}

	public void printPreviousBoards()
	{
		for (int i=0; i<=this.m_numBackups; i++)
		{
			printBoard(i);
			if (i<this.m_numBackups)
			{
				System.out.println("Move: " + ChessBoardConversion.getSimpleAlgebraicMoveFromCompactMove(this.m_moveList[i]) + " (" + this.m_moveList[i] + ")");
			}
		}
	}
	
	public void printBoard(int boardNum)
	{
		char board[] = new char[64];
		char pieces[] = new char[] {'P','N','B','Q','K','R','p','n','b','q','k','r'};
		
		System.out.println();
		System.out.println("*************");
		System.out.println((boardNum == this.m_numBackups ? m_isWhiteToMove : m_isWhiteToMoveBackup[boardNum]) ? "White To Move" : "Black To Move");
		System.out.println("*************");
		for (int i=RivalConstants.WP; i<=RivalConstants.BR; i++)
		{
			int[] bitsSet = Bitboards.getSetBits((boardNum == this.m_numBackups ? m_pieceBitboards[i] : m_pieceBitboardsBackup[boardNum][i]));
			for (int j=0; bitsSet[j] != -1; j++)
			{
				board[bitsSet[j]] = pieces[i];
			}
		}
		
		for (int i=63; i>=0; i--)
		{
			System.out.print(board[i]==0 ? '-' : board[i]);
			if (i % 8 == 0)
			{
				System.out.println();
			}
		}
		
		System.out.println(getFen(boardNum));
	}
	
	public void printBoard()
	{
		printBoard(this.m_numBackups);
	}
}
