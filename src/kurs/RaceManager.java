package kurs;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class RaceManager {
    private static String logProps = "debug.properties";
    static Logger log = Logger.getLogger("RaceManager.class");
    private static ResultSet rs;
    private ArrayList<Integer> ids = new ArrayList<Integer>();
    private static DatabaseManager db = new DatabaseManager();
    private static int rowCount = 0;
    private JFrame raceList;
    public DefaultTableModel model;
    private JButton add;
    private JButton edit;
    private JButton delete;
    private JButton pedestal;
    private JToolBar toolBar;
    private JScrollPane scroll;
    private JTable race;
    private JComboBox team;
    private JComboBox driver;
    private JButton filter;
    private JPanel filterPanel;
    private HashSet<Pair> teams = new HashSet<>();
    private HashSet<Pair> drivers = new HashSet<>();
    private Set<String> tracks = new HashSet<>();
    private Set<String> dates = new HashSet<>();
    String[] tmpStrings;
    int selectedRow = -1;

    public void show() 
    {
        raceList = new JFrame("Список гонок");
        raceList.setSize(800, 500);
        raceList.setLocation(100, 100);
        raceList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add = new JButton(new ImageIcon("./img/add (1).png"));
        edit = new JButton(new ImageIcon("./img/edit (1).png"));
        delete = new JButton(new ImageIcon("./img/minus.png"));
        pedestal = new JButton(new ImageIcon("./img/pedestal.png"));
        
        add.setToolTipText("Добавить запись");
        edit.setToolTipText("Изменить запись");
        delete.setToolTipText("Удалить запись");
        pedestal.setToolTipText("Показать призеров");

        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(add);
        toolBar.add(edit);
        toolBar.add(delete);
        toolBar.add(pedestal);
        
        raceList.setLayout(new BorderLayout());
        raceList.add(toolBar, BorderLayout.NORTH);
        
        String [] columnsRace = {"Команда","Пилот","Трасса","Дата","Место","Очки"};
        model = new DefaultTableModel(null, columnsRace);
        race = new JTable(model);
        race.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scroll = new JScrollPane(race);
        raceList.add(scroll, BorderLayout.CENTER);
        
        
        team = new JComboBox(new String[]{"Команда","Пилот","Дата"}); 
        driver = new JComboBox();
        driver.setPreferredSize(new Dimension(200, 30));
        filter = new JButton("Поиск");
        filterPanel = new JPanel();
        filterPanel.add(team);
        filterPanel.add(driver);
        filterPanel.add(filter);
        
        raceList.add(filterPanel, BorderLayout.SOUTH);

        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                rowEditor(false);
                log.info("Запись добавлена");
            }
        });

        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    if(selectedRow==-1) throw new NoRowException();
                    rowEditor(true);
                    log.info("Запись изменена");
                }
                catch(NoRowException ex) { 
                    log.debug("Исключение при попытке удаления: ", ex);
                    JOptionPane.showMessageDialog(raceList, ex.getMessage());
                } 
            }
        });

        pedestal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    if(selectedRow==-1) throw new NoRowException();
                    db.pedestal(""+model.getValueAt(selectedRow, 2), ""+model.getValueAt(selectedRow, 3));
                    while(model.getRowCount()>0) model.removeRow(0);
                    rs = DatabaseManager.resSet;
                    ids = new ArrayList<Integer>();
                    while(rs.next())
                    {
                        String tmpTeam = rs.getString(1);
                        String tmpDriver = rs.getString(2);
                        String tmpTrack = rs.getString(3);
                        String tmpDate = rs.getString(4);
                        model.addRow(new String[]{tmpTeam,tmpDriver,tmpTrack,tmpDate,""+rs.getInt(5),""+rs.getInt(6)});
                        ids.add(rs.getInt(7));
                    }
                }
                catch(NoRowException ex) { 
                    log.debug("Исключение при попытке просмотра тройки лучших: ", ex);
                    JOptionPane.showMessageDialog(raceList, ex.getMessage());
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    if(selectedRow==-1) throw new NoRowException();
                    DatabaseManager.DeleteRowById(ids.get(selectedRow));
                    model.removeRow(selectedRow);
                    updateTable(false);
                    log.info("Запись удалена");
                }
                catch(NoRowException ex) { 
                    log.debug("Исключение при попытке удаления: ", ex);
                    JOptionPane.showMessageDialog(raceList, ex.getMessage());
                } 
                catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });
        
        race.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
        {
            public void valueChanged(ListSelectionEvent e) 
            {
                if (!e.getValueIsAdjusting()) 
                {
                    selectedRow = race.getSelectedRow();
                    log.debug("выбрана строка "+selectedRow);
                }
            }
        });

        team.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updatePanel();
            }
        });

        filter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                
                try
                {
                    int name=1;
                    String teamName = team.getSelectedItem().toString();
                    if(driver.getSelectedItem().toString().isEmpty()) db.GetAll();
                    else switch(teamName)
                    {
                        case("Команда"):
                            for(Pair i : teams)
                            {
                                if(i.getKey().equals(driver.getSelectedItem().toString()))
                                {
                                    name = i.getValue();
                                    break;
                                }
                            }
                            db.FilterByField("teams", ""+name);
                            break;
                        case("Пилот"):
                        for(Pair i : drivers)
                            {
                                if(i.getKey().equals(driver.getSelectedItem().toString()))
                                {
                                    name = i.getValue();
                                    break;
                                }
                            }
                            db.FilterByField("drivers", ""+name);
                            break;
                        case("Дата"):
                            db.FilterByField("Date", driver.getSelectedItem().toString());
                            break;
                    }
                    updateTable(false);
                }
                catch(NullPointerException ex) { JOptionPane.showMessageDialog(raceList, ex.toString());}
                 catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (SecurityException e1) {
                    e1.printStackTrace();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        try {
            DatabaseManager.Conn();
            db.GetAll();
        } catch (NoSuchMethodException | SecurityException | SQLException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        updateTable(true);

        raceList.setVisible(true);
    }
        
    public static void main(String[] args)
    {
        PropertyConfigurator.configure(logProps);
        log.info("Открытие экранной формы");
        new RaceManager().show();
        log.info("Экранная форма открыта");
    }

    private class NoRowException extends Exception
		{
			/**
			 * Исключение вызывается при попытке получения тройки лучших пилотов, 
			 * если не выбрана ни одна строка таблицы
			 */
            
			public NoRowException()
			{
				super("Не выбрано ни одной строки!");
			}
		}

		public class NoNameException extends Exception 
		{
			/**
			 * Исключение вызывается при попытке поиска, если поле поиска не изменено
			 */
			public NoNameException() 
			{
				super ("Вы не ввели имя для поиска!");
			}
		}

		public void checkName (JTextField bName) throws NoNameException,NullPointerException
		{
			String sName = bName.getText();
			if (sName.contains("Имя пилота")) throw new NoNameException();
			if (sName.length() == 0) throw new NullPointerException();
		}  

        public void updatePanel()
        {
            int k = 1;
            switch(team.getSelectedItem().toString())
            {
                case("Команда"):
                    tmpStrings = new String[teams.size()+1];
                    for(Pair i : teams)
                    {
                        tmpStrings[k++] = i.getKey();
                    }
                    break;
                    case("Пилот"):
                    tmpStrings = new String[drivers.size()+1];
                    for(Pair i : drivers)
                    {
                        tmpStrings[k++] = i.getKey();
                    }
                    break;
                    case("Дата"):
                    tmpStrings = new String[dates.size()+1];
                    for(int i = 0; i< dates.size(); i++) tmpStrings[i] = dates.toArray(new String[0])[i];
                    break;
                }
            tmpStrings[0] = "";
            filterPanel.remove(driver);
            filterPanel.remove(filter);
            driver = new JComboBox(tmpStrings);
            filterPanel.add(driver);
            filterPanel.add(filter);
            filterPanel.revalidate();
            filterPanel.repaint();
        }

        public void updateTable(boolean panel){
            try
            {
                db.Repeat();
                rs = DatabaseManager.resSet;
                ids = new ArrayList<Integer>();
                while(model.getRowCount()>0) model.removeRow(0);
                rowCount = 0;
                while(rs.next())
                {
                    String tmpTeam = rs.getString(1);
                    String tmpDriver = rs.getString(2);
                    String tmpTrack = rs.getString(3);
                    String tmpDate = rs.getString(4);
                    teams.add(new Pair(tmpTeam, rs.getInt(8)));
                    drivers.add(new Pair(tmpDriver, rs.getInt(9)));
                    tracks.add(tmpTrack);
                    dates.add(tmpDate);
                    model.addRow(new String[]{tmpTeam,tmpDriver,tmpTrack,tmpDate,""+rs.getInt(5),""+rs.getInt(6)});
                    rowCount++;
                    ids.add(rs.getInt(7));
                }
                if(panel)
                {    
                    updatePanel();
                }
                log.info("Данные прочитаны");
            }
            catch(SQLException e) { log.debug(e);} 
            catch (SecurityException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        private void rowEditor(boolean edit)
        {
            JDialog inputDialog = new JDialog();
            inputDialog.setSize(300, 300);
            inputDialog.setLayout(new GridLayout(0, 2));
            int k = 1;
            JLabel driverLabel = new JLabel("Имя");
            tmpStrings = new String[drivers.size()+1];
            tmpStrings[0]="";
            for(Pair i : drivers)
            {
                tmpStrings[k++] = i.getKey();
            }
            JComboBox driverName = new JComboBox<>(tmpStrings);
            driverName.setEditable(true);
            if(edit)
            {
                k=0;
                for(String i : tmpStrings)
                {
                    if(i.equals(model.getValueAt(selectedRow,1))) break;
                    k++;
                }
                driverName.setSelectedIndex(k);
            }
            k = 1;
            JLabel teamLabel = new JLabel("Команда");
            tmpStrings = new String[teams.size()+1];
            tmpStrings[0]="";
            for(Pair i : teams)
            {
                tmpStrings[k++] = i.getKey();
            }
            JComboBox teamName = new JComboBox<>(tmpStrings);
            teamName.setEditable(true);
            if(edit)
            {
                k=0;
                for(String i : tmpStrings)
                {
                    if(i.equals(model.getValueAt(selectedRow,0))) break;
                    k++;
                }
                teamName.setSelectedIndex(k);
            }

            JLabel trackLabel = new JLabel("Трасса");
            tmpStrings = new String[tracks.size()+1];
            tmpStrings[0]="";if(tracks.size() > 0) System.arraycopy(tracks.toArray(new String[0]), 0, tmpStrings, 1, tracks.size());
            JComboBox trackName = new JComboBox<>(tmpStrings);
            trackName.setEditable(true);
            if(edit)
            {
                k=0;
                for(String i : tmpStrings)
                {
                    if(i.equals(model.getValueAt(selectedRow,2))) break;
                    k++;
                }
                trackName.setSelectedIndex(k);
            }

            JLabel dateLabel = new JLabel("Дата");
            JTextField dateField = new JTextField(20);
            if(edit) dateField.setText(""+model.getValueAt(selectedRow, 3));

            JLabel placeLabel = new JLabel("Место");
            JTextField placeField = new JTextField(20);
            if(edit) placeField.setText(""+model.getValueAt(selectedRow, 4));

            JLabel pointsLabel = new JLabel("Очки");
            JTextField pointsField = new JTextField(20);
            if(edit) pointsField.setText(""+model.getValueAt(selectedRow, 5));

            JButton submitButton = new JButton("Готово");

            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String teamN = teamName.getSelectedItem().toString();
                    for(Pair i : teams)
                    {
                        if(i.getKey().equals(teamName.getSelectedItem().toString()))
                        {
                            teamN = i.getKey();
                            break;
                        }
                    }
                    String driverN = driverName.getSelectedItem().toString();
                    for(Pair i : drivers)
                    {
                        if(i.getKey().equals(driverName.getSelectedItem().toString()))
                        {
                            driverN = i.getKey();
                            break;
                        }
                    }
                    String trackN = trackName.getSelectedItem().toString();

                    try {
                        if(edit) db.AddRow(teamN, driverN, trackN, dateField.getText(), placeField.getText(), pointsField.getText(), edit,ids.get(selectedRow));
                        else db.AddRow(teamN, driverN, trackN, dateField.getText(), placeField.getText(), pointsField.getText(), edit,0);
                        updateTable(true);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    inputDialog.dispose();
                }
            });
    
            inputDialog.add(teamLabel);
            inputDialog.add(teamName);
            inputDialog.add(driverLabel);
            inputDialog.add(driverName);
            inputDialog.add(trackLabel);
            inputDialog.add(trackName);
            inputDialog.add(dateLabel);
            inputDialog.add(dateField);
            inputDialog.add(placeLabel);
            inputDialog.add(placeField);
            inputDialog.add(pointsLabel);
            inputDialog.add(pointsField);
            inputDialog.add(submitButton);
    
            inputDialog.setVisible(true);
        }

}
