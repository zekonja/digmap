package hr.apis_it.fin._2012.types.f73;

public class Artikl {

	private String sifra;
	private String naziv;
	private String jedMjere;
	private int kolicina=1;
	private String grupa;
	private String opis;
	private PorezType pdv;
	private PorezType pnp;
	private PorezOstaloType po;
	private NaknadaType naknada;
	
	public Artikl(String sifra, String naziv, String jedMjere,
			String grupa, String opis, PorezType pdv, PorezType pnp,
			PorezOstaloType po, NaknadaType naknada) {
		super();
		this.sifra = sifra;
		this.naziv = naziv;
		this.jedMjere = jedMjere;
		this.grupa = grupa;
		this.opis = opis;
		this.pdv = pdv;
		this.pnp = pnp;
		this.po = po;
		this.naknada = naknada;
	}

	
	public String getSifra() {
		return sifra;
	}
	public void setSifra(String sifra) {
		this.sifra = sifra;
	}
	public String getNaziv() {
		return naziv;
	}
	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}
	public String getJedMjere() {
		return jedMjere;
	}
	public void setJedMjere(String jedMjere) {
		this.jedMjere = jedMjere;
	}
	public int getKolicina() {
		return kolicina;
	}
	public void setKolicina(int kolicina) {
		this.kolicina = kolicina;
	}	
	public String getGrupa() {
		return grupa;
	}
	public void setGrupa(String grupa) {
		this.grupa = grupa;
	}
	public String getOpis() {
		return opis;
	}
	public void setOpis(String opis) {
		this.opis = opis;
	}
	public PorezType getPdv() {
		return pdv;
	}
	public void setPdv(PorezType pdv) {
		this.pdv = pdv;
	}
	public PorezType getPnp() {
		return pnp;
	}
	public void setPnp(PorezType pnp) {
		this.pnp = pnp;
	}
	public PorezOstaloType getPo() {
		return po;
	}
	public void setPo(PorezOstaloType po) {
		this.po = po;
	}
	public NaknadaType getNaknada() {
		return naknada;
	}
	public void setNaknada(NaknadaType naknada) {
		this.naknada = naknada;
	}	
}
